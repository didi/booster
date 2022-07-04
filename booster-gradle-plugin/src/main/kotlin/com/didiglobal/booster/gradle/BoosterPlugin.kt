package com.didiglobal.booster.gradle

import com.android.build.gradle.AppExtension
import com.android.build.gradle.BaseExtension
import com.android.build.gradle.LibraryExtension
import com.android.build.gradle.api.BaseVariant
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

        if (!GTE_V3_6) {
            project.gradle.addListener(BoosterTransformTaskExecutionListener(project))
        }

        val android = project.getAndroid<BaseExtension>()
        when (android) {
            is AppExtension -> android.applicationVariants
            is LibraryExtension -> android.libraryVariants
            else -> emptyList<BaseVariant>()
        }.takeIf<Collection<BaseVariant>>(Collection<BaseVariant>::isNotEmpty)?.let { variants ->
            android.registerTransform(BoosterTransform.newInstance(project))
            if (project.state.executed) {
                project.setup(variants)
            } else {
                project.afterEvaluate {
                    project.setup(variants)
                }
            }
        }
    }

    private fun Project.setup(variants: Collection<BaseVariant>) {
        val processors = loadVariantProcessors(this)
        variants.forEach { variant ->
            processors.forEach { processor ->
                processor.process(variant)
            }
        }
    }


}
