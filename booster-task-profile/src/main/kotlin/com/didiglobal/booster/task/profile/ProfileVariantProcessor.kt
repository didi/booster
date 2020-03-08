package com.didiglobal.booster.task.profile

import com.android.build.gradle.api.BaseVariant
import com.android.build.gradle.internal.pipeline.TransformTask
import com.didiglobal.booster.gradle.extension
import com.didiglobal.booster.gradle.project
import com.didiglobal.booster.task.spi.VariantProcessor
import com.google.auto.service.AutoService

/**
 * @author johnsonlee
 */
@AutoService(VariantProcessor::class)
class ProfileVariantProcessor : VariantProcessor {

    override fun process(variant: BaseVariant) {
        val project = variant.project
        val variantName = variant.name.capitalize()
        val lastTransform = variant.extension.transforms.last()
        project.tasks.withType(TransformTask::class.java).find {
            it.name.endsWith(variantName) && it.transform == lastTransform
        }?.let { transform ->
            project.tasks.create("profile${variantName}", ProfileTask::class.java) {
                it.variant = variant
                it.supplier = { transform.outputs.files.single() }
            }.dependsOn(transform)
        }
    }

}