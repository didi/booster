package com.didiglobal.booster.transform.toast

import com.android.build.gradle.api.BaseVariant
import com.android.build.gradle.api.LibraryVariant
import com.didiglobal.booster.gradle.isDynamicFeature
import com.didiglobal.booster.gradle.scope
import com.didiglobal.booster.gradle.variantData
import com.didiglobal.booster.task.spi.VariantProcessor
import com.google.auto.service.AutoService

@AutoService(VariantProcessor::class)
class ToastVariantProcessor : VariantProcessor {

    override fun process(variant: BaseVariant) {
        if (variant !is LibraryVariant && !variant.variantData.isDynamicFeature()) {
            variant.scope.globalScope.project.dependencies.add("implementation", "${Build.GROUP}:booster-android-instrument-toast:${Build.VERSION}")
        }
    }

}
