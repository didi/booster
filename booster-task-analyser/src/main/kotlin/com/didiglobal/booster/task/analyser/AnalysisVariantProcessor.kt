package com.didiglobal.booster.task.analyser

import com.android.build.gradle.AppExtension
import com.android.build.gradle.BaseExtension
import com.android.build.gradle.LibraryExtension
import com.android.build.gradle.api.BaseVariant
import com.android.build.gradle.internal.tasks.factory.dependsOn
import com.didiglobal.booster.cha.asm.AsmClassSetCache
import com.didiglobal.booster.gradle.getAndroid
import com.didiglobal.booster.gradle.getJarTaskProviders
import com.didiglobal.booster.gradle.getTaskName
import com.didiglobal.booster.gradle.getUpstreamProjects
import com.didiglobal.booster.gradle.isAndroid
import com.didiglobal.booster.gradle.isJava
import com.didiglobal.booster.gradle.isJavaLibrary
import com.didiglobal.booster.gradle.project
import com.didiglobal.booster.task.analyser.performance.PerformanceAnalysisTask
import com.didiglobal.booster.task.analyser.reference.ReferenceAnalysisTask
import com.didiglobal.booster.task.spi.VariantProcessor
import com.google.auto.service.AutoService
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.UnknownTaskException
import org.gradle.api.tasks.TaskProvider
import java.util.Locale
import kotlin.reflect.KClass

@AutoService(VariantProcessor::class)
class AnalysisVariantProcessor : VariantProcessor {

    private val classSetCache = AsmClassSetCache()

    override fun process(variant: BaseVariant) {
        variant.project.gradle.projectsEvaluated { gradle ->
            gradle.rootProject.allprojects {
                it.setup()
            }
        }
    }

    private fun Project.setup() {
        when {
            isAndroid -> {
                setupAndroid<PerformanceAnalysisTask>()
                setupAndroid<ReferenceAnalysisTask>()
            }
            isJavaLibrary || isJava -> {
                setupTasks<ReferenceAnalysisTask>()
            }
        }
    }

    private inline fun <reified T : AnalysisTask> Project.setupTasks(variant: BaseVariant? = null): TaskProvider<out Task> {
        val taskName = variant?.getTaskName(T::class.taskName) ?: T::class.taskName
        return try {
            tasks.named(taskName)
        } catch (e: UnknownTaskException) {
            tasks.register(taskName, T::class.java) {
                it.variant = variant
                it.classSetCache = classSetCache
            }.dependsOn(getUpstreamProjects(false, variant).plus(this).map {
                it.getJarTaskProviders(variant)
            }.flatten())
        }
    }

    private inline fun <reified T : AnalysisTask> Project.setupAndroid() {
        when (val android = getAndroid<BaseExtension>()) {
            is LibraryExtension -> android.libraryVariants
            is AppExtension -> android.applicationVariants
            else -> emptyList<BaseVariant>()
        }.map {
            setupTasks<T>(it)
        }
    }

}

internal inline val <reified T : AnalysisTask> KClass<T>.category: String
    get() = T::class.java.simpleName.substringBefore(AnalysisTask::class.java.simpleName).lowercase()

internal inline val <reified T : AnalysisTask> KClass<T>.taskName: String
    get() = @Suppress("DEPRECATION") "analyse${category.capitalize()}"
