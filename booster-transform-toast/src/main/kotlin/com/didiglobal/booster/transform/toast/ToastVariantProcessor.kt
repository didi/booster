package com.didiglobal.booster.transform.toast

import com.android.build.api.variant.Variant
import com.didiglobal.booster.gradle.isDynamicFeature
import com.didiglobal.booster.gradle.isLibrary
import com.didiglobal.booster.gradle.project
import com.didiglobal.booster.task.spi.VariantProcessor
import com.google.auto.service.AutoService

@AutoService(VariantProcessor::class)
class ToastVariantProcessor : VariantProcessor {

    override fun process(variant: Variant) {
        if (!variant.isLibrary && !variant.isDynamicFeature) {
            variant.project.dependencies.add("implementation", "${Build.GROUP}:booster-android-instrument-toast:${Build.VERSION}")
        }
    }

}
