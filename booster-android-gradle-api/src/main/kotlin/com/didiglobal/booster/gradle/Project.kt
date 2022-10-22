package com.didiglobal.booster.gradle

import com.android.build.gradle.AppExtension
import com.android.build.gradle.BaseExtension
import com.android.build.gradle.LibraryExtension
import com.android.build.gradle.api.BaseVariant
import com.android.repository.Revision
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.artifacts.component.ComponentArtifactIdentifier
import org.gradle.api.artifacts.component.ComponentIdentifier
import org.gradle.api.artifacts.component.ProjectComponentIdentifier
import org.gradle.api.artifacts.result.ResolvedArtifactResult
import org.gradle.api.artifacts.result.ResolvedVariantResult
import org.gradle.api.attributes.Attribute
import org.gradle.api.attributes.AttributeContainer
import org.gradle.api.capabilities.Capability
import org.gradle.api.component.Artifact
import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.plugins.JavaPlugin.RUNTIME_CLASSPATH_CONFIGURATION_NAME
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.TaskProvider
import org.gradle.maven.MavenPomArtifact
import java.io.File
import java.util.Optional
import java.util.Stack

/**
 * The gradle version
 */
val Project.gradleVersion: Revision
    get() = Revision.parseRevision(gradle.gradleVersion)

@Deprecated(
        message = "Use isAapt2Enabled instead",
        replaceWith = ReplaceWith(
                expression = "isAapt2Enabled"
        )
)
val Project.aapt2Enabled: Boolean
    get() = AGP.run { isAapt2Enabled }

val Project.isAapt2Enabled: Boolean
    get() = AGP.run { isAapt2Enabled }

val Project.isAndroid: Boolean
    get() = plugins.hasPlugin("com.android.application")
            || plugins.hasPlugin("com.android.dynamic-feature")
            || plugins.hasPlugin("com.android.library")

val Project.isJava: Boolean
    get() = plugins.hasPlugin("java") || isJavaLibrary

val Project.isJavaLibrary: Boolean
    get() = plugins.hasPlugin("java-library")

@Suppress("UNCHECKED_CAST")
fun <T> Project.getProperty(name: String, defaultValue: T): T {
    val value = findProperty(name) ?: return defaultValue
    return when (defaultValue) {
        is Boolean -> if (value is Boolean) value as T else value.toString().toBoolean() as T
        is Byte -> if (value is Byte) value as T else value.toString().toByte() as T
        is Short -> if (value is Short) value as T else value.toString().toShort() as T
        is Int -> if (value is Int) value as T else value.toString().toInt() as T
        is Float -> if (value is Float) value as T else value.toString().toFloat() as T
        is Long -> if (value is Long) value as T else value.toString().toLong() as T
        is Double -> if (value is Double) value as T else value.toString().toDouble() as T
        is String -> if (value is String) value as T else value.toString() as T
        else -> value as T
    }
}

fun Project.getUpstreamProjects(
        transitive: Boolean = true,
        variant: BaseVariant? = null
): Set<Project> = getResolvedArtifactResults(transitive, variant).mapNotNull {
    (it.id.componentIdentifier as? ProjectComponentIdentifier)?.projectPath?.let { projectPath ->
        rootProject.project(projectPath)
    }
}.toSet()

fun Project.getResolvedArtifactResults(
        transitive: Boolean = true,
        variant: BaseVariant? = null
): Set<ResolvedArtifactResult> = when {
    isAndroid -> getResolvedArtifactResultsRecursively(transitive) {
        filterByVariant(variant).map { v ->
            AGP.run { v.getDependencies(transitive) }
        }.flatten()
    }
    isJava -> getResolvedArtifactResultsRecursively(transitive) {
        configurations.getByName(RUNTIME_CLASSPATH_CONFIGURATION_NAME).resolvedConfiguration.resolvedArtifacts.map {
            ResolvedArtifactResultImpl(it.id, it.file)
        }
    }
    else -> emptySet()
}.distinctBy {
    it.id.componentIdentifier
}.toSet()

private fun Project.getResolvedArtifactResultsRecursively(transitive: Boolean, resolve: Project.() -> List<ResolvedArtifactResult>): Set<ResolvedArtifactResult> {
    val stack = Stack<Project>()
    val results = mutableMapOf<ComponentIdentifier, ResolvedArtifactResult>()

    stack.add(this)

    while (stack.isNotEmpty()) {
        val resolved = stack.pop().resolve().filterNot {
            results.containsKey(it.id.componentIdentifier)
        }.onEach {
            results[it.id.componentIdentifier] = it
        }

        if (!transitive) continue

        resolved.map {
            it.id.componentIdentifier
        }.filterIsInstance<ProjectComponentIdentifier>().map {
            rootProject.project(it.projectPath)
        }.let(stack::addAll)
    }

    return results.values.toSet()
}

/**
 * Returns the jar files which could be the outputs of the jar task or createFullJar task
 *
 * @param variant The build variant
 */
fun Project.getJars(variant: BaseVariant? = null): Set<File> = getJarTaskProviders(variant).map {
    it.get().outputs.files
}.flatten().toSet()

fun Project.getJarTaskProviders(variant: BaseVariant? = null): Collection<TaskProvider<out Task>> = when {
    isAndroid -> when (getAndroid<BaseExtension>()) {
        is LibraryExtension -> filterByVariant(variant).mapNotNull(BaseVariant::createFullJarTaskProvider)
        is AppExtension -> filterByVariant(variant).mapNotNull(BaseVariant::bundleClassesTaskProvider)
        else -> emptyList()
    }
    isJavaLibrary -> listOf(tasks.named(JavaPlugin.JAR_TASK_NAME))
    else -> emptyList()
}

private fun Project.filterByVariant(variant: BaseVariant? = null): Collection<BaseVariant> {
    val variants = when (val android = getAndroid<BaseExtension>()) {
        is AppExtension -> android.applicationVariants
        is LibraryExtension -> android.libraryVariants
        else -> emptyList<BaseVariant>()
    }

    if (null == variant) return variants

    return variants.filter {
        it.name == variant.name
    }.takeIf {
        it.isNotEmpty()
    } ?: variants.filter {
        it.buildType.name == variant.buildType.name
    }
}

private data class ResolvedArtifactResultImpl(
        private val artifactId: ComponentArtifactIdentifier,
        private val artifactFile: File
) : ResolvedArtifactResult {

    private val variant by lazy {
        object : ResolvedVariantResult {
            override fun getAttributes(): AttributeContainer = EmptyAttributes
            override fun getDisplayName(): String = id.displayName
            override fun getCapabilities(): MutableList<Capability> = mutableListOf()
            override fun getOwner(): ComponentIdentifier = artifactId.componentIdentifier
            override fun getExternalVariant(): Optional<ResolvedVariantResult> = Optional.empty()
        }
    }

    override fun getId(): ComponentArtifactIdentifier = artifactId
    override fun getType(): Class<out Artifact> = MavenPomArtifact::class.java
    override fun getFile(): File = artifactFile
    override fun getVariant(): ResolvedVariantResult = variant
}

private object EmptyAttributes : AttributeContainer {
    override fun getAttributes(): AttributeContainer = this
    override fun keySet(): MutableSet<Attribute<*>> = mutableSetOf()
    override fun <T : Any?> attribute(key: Attribute<T>, value: T): AttributeContainer = this
    override fun <T : Any?> getAttribute(key: Attribute<T>): T? = null
    override fun isEmpty(): Boolean = true
    override fun contains(key: Attribute<*>): Boolean = false
    override fun <T : Any?> attributeProvider(key: Attribute<T>, provider: Provider<out T>): AttributeContainer = this
}