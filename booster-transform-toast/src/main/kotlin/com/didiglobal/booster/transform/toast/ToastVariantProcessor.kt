package com.didiglobal.booster.transform.toast

import com.android.build.gradle.api.BaseVariant
import com.android.build.gradle.api.LibraryVariant
import com.didiglobal.booster.gradle.project
import com.didiglobal.booster.gradle.variantType
import com.didiglobal.booster.task.spi.VariantProcessor
import com.google.auto.service.AutoService

@AutoService(VariantProcessor::class)
class ToastVariantProcessor : VariantProcessor {

    override fun process(variant: BaseVariant) {
        if (variant !is LibraryVariant && !variant.variantType.isDynamicFeature) {
            variant.project.dependencies.add("implementation", "${Build.GROUP}:booster-android-instrument-toast:${Build.VERSION}")
        }
    }

}
