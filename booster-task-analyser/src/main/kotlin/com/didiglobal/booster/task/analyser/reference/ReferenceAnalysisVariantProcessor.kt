package com.didiglobal.booster.task.analyser.reference

import com.android.build.gradle.AppExtension
import com.android.build.gradle.BaseExtension
import com.android.build.gradle.LibraryExtension
import com.android.build.gradle.api.BaseVariant
import com.android.build.gradle.internal.tasks.factory.dependsOn
import com.didiglobal.booster.BOOSTER
import com.didiglobal.booster.gradle.getAndroid
import com.didiglobal.booster.gradle.getJarTaskProviders
import com.didiglobal.booster.gradle.isAndroid
import com.didiglobal.booster.gradle.isJava
import com.didiglobal.booster.gradle.isJavaLibrary
import com.didiglobal.booster.gradle.project
import com.didiglobal.booster.task.analyser.configureReportConvention
import com.didiglobal.booster.task.spi.VariantProcessor
import com.google.auto.service.AutoService
import org.gradle.api.Project
import org.gradle.api.UnknownTaskException
import org.gradle.api.tasks.TaskProvider

@AutoService(VariantProcessor::class)
class ReferenceAnalysisVariantProcessor : VariantProcessor {

    override fun process(variant: BaseVariant) {
        variant.project.gradle.projectsEvaluated { gradle ->
            val prerequisites = gradle.rootProject.allprojects.map { project ->
                project.getJarTaskProviders(variant)
            }.flatten()
            gradle.rootProject.allprojects { project ->
                project.setup(prerequisites)
            }
        }
    }

}

private fun Project.setup(prerequisites: List<TaskProvider<*>>) {
    try {
        project.tasks.named(TASK_ANALYSE_REFERENCE)
    } catch (e: UnknownTaskException) {
        when {
            isAndroid -> setupAndroid(prerequisites)
            isJavaLibrary || isJava -> setupJava(prerequisites)
        }
    }
}

private fun Project.setupJava(prerequisites: List<TaskProvider<*>>) {
    tasks.register(TASK_ANALYSE_REFERENCE, ReferenceAnalysisTask::class.java) {
        it.group = BOOSTER
        it.description = "Analyses class reference for Java projects"
        it.variant = null
        it.configureReportConvention("reference", null)
    }.dependsOn(prerequisites)
}

private fun Project.setupAndroid(prerequisites: List<TaskProvider<*>>) {
    val subtasks = when (val android = getAndroid<BaseExtension>()) {
        is LibraryExtension -> android.libraryVariants
        is AppExtension -> android.applicationVariants
        else -> emptyList<BaseVariant>()
    }.map { variant ->
        tasks.register("${TASK_ANALYSE_REFERENCE}${variant.name.capitalize()}", ReferenceAnalysisTask::class.java) {
            it.group = BOOSTER
            it.description = "Analyses class reference for ${variant.name}"
            it.variant = variant
            it.configureReportConvention("reference", variant)
        }.dependsOn(prerequisites)
    }
    tasks.register(TASK_ANALYSE_REFERENCE) {
        it.group = BOOSTER
        it.description = "Analyses class reference"
    }.dependsOn(subtasks)
}
