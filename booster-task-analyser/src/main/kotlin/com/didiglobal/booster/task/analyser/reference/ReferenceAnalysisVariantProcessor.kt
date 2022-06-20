package com.didiglobal.booster.task.analyser.reference

import com.android.build.gradle.AppExtension
import com.android.build.gradle.BaseExtension
import com.android.build.gradle.LibraryExtension
import com.android.build.gradle.api.BaseVariant
import com.android.build.gradle.internal.tasks.factory.dependsOn
import com.didiglobal.booster.BOOSTER
import com.didiglobal.booster.gradle.getAndroid
import com.didiglobal.booster.gradle.getJarTaskProviders
import com.didiglobal.booster.gradle.getTaskName
import com.didiglobal.booster.gradle.isAndroid
import com.didiglobal.booster.gradle.isJava
import com.didiglobal.booster.gradle.isJavaLibrary
import com.didiglobal.booster.gradle.project
import com.didiglobal.booster.task.analyser.configureReportConvention
import com.didiglobal.booster.task.spi.VariantProcessor
import com.google.auto.service.AutoService
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.UnknownTaskException
import org.gradle.api.tasks.TaskProvider

@AutoService(VariantProcessor::class)
class ReferenceAnalysisVariantProcessor : VariantProcessor {

    override fun process(variant: BaseVariant) {
        variant.project.gradle.projectsEvaluated { gradle ->
            gradle.rootProject.allprojects(Project::setup)
        }
    }

}

private fun Project.setup() {
    try {
        project.tasks.named(TASK_ANALYSE_REFERENCE)
    } catch (e: UnknownTaskException) {
        when {
            isAndroid -> setupAndroid()
            isJavaLibrary || isJava -> setupTasks()
        }
    }
}

private fun Project.setupTasks(variant: BaseVariant? = null): TaskProvider<out Task> {
    val taskName = variant?.getTaskName(TASK_ANALYSE_REFERENCE) ?: TASK_ANALYSE_REFERENCE
    return tasks.register(taskName, ReferenceAnalysisTask::class.java) {
        it.group = BOOSTER
        it.description = "Analyses class reference for ${if (variant != null) variant.name else "Java projects"}"
        it.variant = null
        it.configureReportConvention("reference", null)
    }.dependsOn(rootProject.allprojects.map { project ->
        project.getJarTaskProviders(variant)
    }.flatten())
}

private fun Project.setupAndroid() {
    val subtasks = when (val android = getAndroid<BaseExtension>()) {
        is LibraryExtension -> android.libraryVariants
        is AppExtension -> android.applicationVariants
        else -> emptyList<BaseVariant>()
    }.map(::setupTasks)
    tasks.register(TASK_ANALYSE_REFERENCE) {
        it.group = BOOSTER
        it.description = "Analyses class reference"
    }.dependsOn(subtasks)
}
