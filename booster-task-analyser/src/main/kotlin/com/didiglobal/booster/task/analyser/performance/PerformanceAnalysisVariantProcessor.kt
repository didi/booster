package com.didiglobal.booster.task.analyser.performance

import com.android.build.api.transform.QualifiedContent
import com.android.build.gradle.api.BaseVariant
import com.android.build.gradle.internal.pipeline.TransformManager
import com.android.build.gradle.internal.pipeline.TransformTask
import com.android.build.gradle.internal.tasks.factory.dependsOn
import com.didiglobal.booster.BOOSTER
import com.didiglobal.booster.gradle.extension
import com.didiglobal.booster.gradle.project
import com.didiglobal.booster.task.analyser.configureReportConvention
import com.didiglobal.booster.task.spi.VariantProcessor
import com.google.auto.service.AutoService
import org.gradle.api.GradleException
import org.gradle.api.UnknownTaskException

/**
 * @author johnsonlee
 */
@AutoService(VariantProcessor::class)
class PerformanceAnalysisVariantProcessor : VariantProcessor {

    override fun process(variant: BaseVariant) {
        val project = variant.project
        val variantName = variant.name.capitalize()
        val transform = variant.extension.transforms.reversed().firstOrNull {
            it.scopes.containsAll(TransformManager.SCOPE_FULL_PROJECT)
                    && it.inputTypes.contains(QualifiedContent.DefaultContentType.CLASSES)
        } ?: throw GradleException("No available transform")
        val profiles = try {
            project.tasks.named(TASK_ANALYSE_PERFORMANCE)
        } catch (e: UnknownTaskException) {
            project.tasks.register(TASK_ANALYSE_PERFORMANCE) {
                it.description = "Analyses performance issues for Android projects"
                it.group = BOOSTER
            }
        }

        project.tasks.withType(TransformTask::class.java).find {
            it.name.endsWith(variantName) && it.transform == transform
        }?.let { transformTask ->
            val profile = project.tasks.register("${TASK_ANALYSE_PERFORMANCE}${variantName}", PerformanceAnalysisTask::class.java) {
                it.description = "Analyses performance issues for ${variant.name}"
                it.group = BOOSTER
                it.variant = variant
                it.supplier = {
                    transformTask.outputs.files.single()
                }
                it.dependsOn(transformTask)
                it.configureReportConvention("performance", variantName)
            }
            profiles.dependsOn(profile)
        }
    }

}