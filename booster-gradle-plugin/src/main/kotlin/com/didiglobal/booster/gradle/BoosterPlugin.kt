package com.didiglobal.booster.gradle

import com.android.build.api.artifact.ScopedArtifact
import com.android.build.api.variant.AndroidComponentsExtension
import com.android.build.api.variant.ScopedArtifacts
import com.android.build.gradle.AppExtension
import com.android.build.gradle.BaseExtension
import com.android.build.gradle.LibraryExtension
import com.android.build.gradle.api.BaseVariant
import com.didiglobal.booster.kotlinx.capitalized
import com.didiglobal.booster.task.spi.VariantProcessor
import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project

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
        val android = project.getAndroid<BaseExtension>()
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
        val androidComponents = project.extensions.getByType(AndroidComponentsExtension::class.java)
        androidComponents.onVariants { variant ->
            val transform = project.tasks.register(
                "transform${variant.name.capitalized()}ClassesWithBooster",
                BoosterTransformTask::class.java
            ) {
                it.transformers = loadTransformers(project.buildscript.classLoader)
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
