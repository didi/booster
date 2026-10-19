package com.didiglobal.booster.gradle

import com.android.build.api.artifact.ScopedArtifact
import com.android.build.api.dsl.CommonExtension
import com.android.build.api.instrumentation.InstrumentationScope
import com.android.build.api.variant.AndroidComponentsExtension
import com.android.build.api.variant.LibraryVariant
import com.android.build.api.variant.ScopedArtifacts
import com.android.build.gradle.AppExtension
import com.android.build.gradle.BaseExtension
import com.android.build.gradle.LibraryExtension
import com.android.build.gradle.api.BaseVariant
import com.didiglobal.booster.kotlinx.capitalized
import com.didiglobal.booster.task.spi.VariantProcessor
import com.didiglobal.booster.transform.CompositeTransformer
import com.didiglobal.booster.transform.TransformListener
import com.didiglobal.booster.transform.TransformScope
import com.didiglobal.booster.transform.Transformer
import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.result.ResolvedComponentResult
import org.gradle.api.artifacts.result.ResolvedDependencyResult

/**
 * Represents the booster gradle plugin
 *
 * @author johnsonlee
 */
class BoosterPlugin : Plugin<Project> {

    override fun apply(project: Project) {
        project.extensions.findByName("android") ?: throw GradleException("$project is not an Android project")
        registerTransform(project)
        setupTasks(project)
    }

    private fun setupTasks(project: Project) {
        val processors = loadVariantProcessors(project)
        project.setup(processors)

        if (project.state.executed) {
            project.legacySetup(processors)
        } else {
            project.afterEvaluate {
                project.legacySetup(processors)
            }
        }
    }

    private fun Project.setup(processors: List<VariantProcessor>) {
        val androidComponents = project.extensions.getByType(AndroidComponentsExtension::class.java)
        androidComponents.beforeVariants { variantBuilder ->
            processors.forEach { processor ->
                processor.beforeProcess(variantBuilder)
            }
        }
        androidComponents.onVariants { variant ->
            processors.forEach { processor ->
                processor.process(variant)
            }
        }
    }

    private fun Project.legacySetup(processors: List<VariantProcessor>) {
        val android = extensions.findByName("android") as? BaseExtension ?: return
        when (android) {
            is AppExtension -> android.applicationVariants
            is LibraryExtension -> android.libraryVariants
            else -> emptyList<BaseVariant>()
        }.takeIf<Collection<BaseVariant>>(Collection<BaseVariant>::isNotEmpty)?.let { variants ->
            variants.forEach { variant ->
                processors.forEach { processor ->
                    processor.process(variant)
                }
            }
        }
    }

    private fun registerTransform(project: Project) {
        val classLoader = project.buildscript.classLoader
        if (lookupTransformers(classLoader).isEmpty()) {
            // Registering a class transformation without transformer only costs build time,
            // see https://github.com/didi/booster/issues/498
            project.logger.info("No transformer found for ${project.path}, skip registering class transformation")
            return
        }

        val transformers = loadTransformers(classLoader)
        val mode = resolveTransformMode(project.findProperty(PROPERTY_TRANSFORM_MODE)?.toString(), transformers)
        project.logger.lifecycle(describeTransformScope(project.path, mode, transformers))
        when (mode) {
            TRANSFORM_MODE_INSTRUMENTATION -> registerInstrumentation(project, classLoader)
            TRANSFORM_MODE_SCOPED_ARTIFACTS -> registerScopedArtifactsTransform(project, classLoader)
        }
    }

    /**
     * Transforms classes one by one with AGP instrumentation, the boundaries of the inputs are preserved,
     * so that only the changed classes/jars are re-instrumented and re-dexed in incremental builds.
     */
    private fun registerInstrumentation(project: Project, classLoader: ClassLoader) {
        val androidComponents = project.extensions.getByType(AndroidComponentsExtension::class.java)
        val service = project.gradle.sharedServices.registerIfAbsent(
            BoosterInstrumentationService.nameOf(project.path),
            BoosterInstrumentationService::class.java
        ) {}

        androidComponents.onVariants { variant ->
            service.get().register(project.path, variant.name, loadTransformers(classLoader))

            val scope = if (variant is LibraryVariant) InstrumentationScope.PROJECT else InstrumentationScope.ALL
            variant.instrumentation.transformClassesWith(BoosterAsmClassVisitorFactory::class.java, scope) { params ->
                params.projectPath.set(project.path)
                params.variantName.set(variant.name)
                params.applicationId.set(variant.namespace)
                params.debuggable.set(variant.isDebuggable)
                params.dataBindingEnabled.set(project.isDataBindingEnabled)
                params.properties.set(project.boosterProperties)
                params.dependencies.set(variant.runtimeConfiguration.incoming.resolutionResult.rootComponent.map(ResolvedComponentResult::dependencyIds))
                params.projectDir.set(project.layout.projectDirectory)
                params.bootClasspath.set(androidComponents.sdkComponents.bootClasspath)
                params.mergedManifests.from(variant.mergedManifests)
                params.symbolList.from(variant.symbolList)
                params.service.set(service)
            }
        }
    }

    /**
     * Transforms all classes into a single jar with [ScopedArtifacts], a change of single class causes
     * the whole jar to be re-dexed in incremental builds.
     */
    private fun registerScopedArtifactsTransform(project: Project, classLoader: ClassLoader) {
        val androidComponents = project.extensions.getByType(AndroidComponentsExtension::class.java)
        androidComponents.onVariants { variant ->
            val transform = project.tasks.register(
                "transform${variant.name.capitalized()}ClassesWithBooster",
                BoosterTransformTask::class.java
            ) {
                it.transformers = loadTransformers(classLoader)
                it.variant = variant
                it.applicationId = variant.namespace.get()
                it.bootClasspath = androidComponents.sdkComponents.bootClasspath
            }
            variant.artifacts.forScope(ScopedArtifacts.Scope.ALL)
                .use(transform).toTransform(
                    ScopedArtifact.CLASSES,
                    BoosterTransformTask::allJars,
                    BoosterTransformTask::allDirectories,
                    BoosterTransformTask::output
                )
        }
    }

}

/**
 * The property to switch the transform mode, [TRANSFORM_MODE_AUTO] by default
 */
const val PROPERTY_TRANSFORM_MODE = "booster.transform.mode"

/**
 * Chooses [TRANSFORM_MODE_INSTRUMENTATION] if all transformers are [TransformScope.CLASS] scoped,
 * otherwise [TRANSFORM_MODE_SCOPED_ARTIFACTS]
 */
const val TRANSFORM_MODE_AUTO = "auto"

const val TRANSFORM_MODE_INSTRUMENTATION = "instrumentation"

const val TRANSFORM_MODE_SCOPED_ARTIFACTS = "scoped-artifacts"

/**
 * Describes the finalized transform scope and the transformers which require [TransformScope.PROGRAM]
 */
internal fun describeTransformScope(projectPath: String, mode: String, transformers: Collection<Transformer>): String {
    val scope = if (transformers.all { it.scope == TransformScope.CLASS }) TransformScope.CLASS else TransformScope.PROGRAM
    val builder = StringBuilder("Booster transform scope of $projectPath is $scope, classes will be transformed in `$mode` mode")
    if (scope == TransformScope.PROGRAM) {
        builder.append(", the whole program is required by:")
        transformers.filter { it.scope == TransformScope.PROGRAM }.forEach {
            it.describe(builder, 1)
        }
        builder.append("\nIncremental builds are slower in `$TRANSFORM_MODE_SCOPED_ARTIFACTS` mode, see https://github.com/didi/booster/issues/498")
    }
    if (mode == TRANSFORM_MODE_INSTRUMENTATION && scope == TransformScope.PROGRAM) {
        builder.append("\nWARNING: `$TRANSFORM_MODE_INSTRUMENTATION` mode is forced by $PROPERTY_TRANSFORM_MODE, the transformers above may not work as expected")
    }
    return builder.toString()
}

private fun TransformListener.describe(builder: StringBuilder, depth: Int) {
    builder.append("\n").append("  ".repeat(depth)).append("- ").append(javaClass.name).append(": ").append(scope)
    if (this is CompositeTransformer) {
        delegates.forEach {
            it.describe(builder, depth + 1)
        }
    }
}

/**
 * Resolves the transform mode from the property value and the scopes of transformers
 */
internal fun resolveTransformMode(mode: String?, transformers: Collection<Transformer>): String = when (mode) {
    null, "", TRANSFORM_MODE_AUTO -> if (transformers.all { it.scope == TransformScope.CLASS }) TRANSFORM_MODE_INSTRUMENTATION else TRANSFORM_MODE_SCOPED_ARTIFACTS
    TRANSFORM_MODE_INSTRUMENTATION, TRANSFORM_MODE_SCOPED_ARTIFACTS -> mode
    else -> throw GradleException("Unsupported $PROPERTY_TRANSFORM_MODE `$mode`, expected `$TRANSFORM_MODE_AUTO`, `$TRANSFORM_MODE_INSTRUMENTATION` or `$TRANSFORM_MODE_SCOPED_ARTIFACTS`")
}

private const val PROPERTY_PREFIX = "booster."

private val Project.boosterProperties: Map<String, String>
    get() = properties.filter { (name, value) ->
        name.startsWith(PROPERTY_PREFIX) && value != null
    }.mapValues {
        it.value.toString()
    }

private val Project.isDataBindingEnabled: Boolean
    get() = try {
        (extensions.findByName("android") as? CommonExtension<*, *, *, *>)?.dataBinding?.enable == true
    } catch (e: Throwable) {
        false
    }

/**
 * Returns the identifiers of all transitive dependencies
 */
private fun ResolvedComponentResult.dependencyIds(): List<String> {
    val visited = LinkedHashSet<String>()
    val queue = ArrayDeque<ResolvedComponentResult>()
    queue.add(this)
    while (queue.isNotEmpty()) {
        queue.removeFirst().dependencies.filterIsInstance<ResolvedDependencyResult>().forEach { dependency ->
            if (visited.add(dependency.selected.id.displayName)) {
                queue.add(dependency.selected)
            }
        }
    }
    return visited.sorted()
}
