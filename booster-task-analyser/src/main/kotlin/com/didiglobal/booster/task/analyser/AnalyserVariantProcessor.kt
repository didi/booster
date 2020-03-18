package com.didiglobal.booster.task.analyser

import com.android.build.api.transform.QualifiedContent
import com.android.build.gradle.api.BaseVariant
import com.android.build.gradle.internal.pipeline.TransformManager
import com.android.build.gradle.internal.pipeline.TransformTask
import com.didiglobal.booster.gradle.extension
import com.didiglobal.booster.gradle.project
import com.didiglobal.booster.task.spi.VariantProcessor
import com.google.auto.service.AutoService
import org.gradle.api.GradleException

/**
 * @author johnsonlee
 */
@AutoService(VariantProcessor::class)
class AnalyserVariantProcessor : VariantProcessor {

    override fun process(variant: BaseVariant) {
        val project = variant.project
        val variantName = variant.name.capitalize()
        val transform = variant.extension.transforms.reversed().first {
            it.scopes.containsAll(TransformManager.SCOPE_FULL_PROJECT)
                    && it.inputTypes.contains(QualifiedContent.DefaultContentType.CLASSES)
        } ?: throw GradleException("No available transform")
        val profiles = project.tasks.findByName("analyse") ?: project.tasks.create("analyse")

        project.tasks.withType(TransformTask::class.java).find {
            it.name.endsWith(variantName) && it.transform == transform
        }?.let { transformTask ->
            val profile = project.tasks.create("analyse${variantName}", AnalyserTask::class.java) {
                it.variant = variant
                it.supplier = {
                    transformTask.outputs.files.single()
                }
            }.dependsOn(transformTask)
            profiles.dependsOn(profile)
        }
    }

}