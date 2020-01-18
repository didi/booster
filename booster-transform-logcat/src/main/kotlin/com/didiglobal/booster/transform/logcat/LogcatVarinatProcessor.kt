package com.didiglobal.booster.transform.logcat

import com.android.build.gradle.api.BaseVariant
import com.android.build.gradle.api.LibraryVariant
import com.didiglobal.booster.gradle.isDynamicFeature
import com.didiglobal.booster.gradle.project
import com.didiglobal.booster.gradle.variantData
import com.didiglobal.booster.task.spi.VariantProcessor
import com.google.auto.service.AutoService

/**
 * @author neighbWang
 */
@AutoService(VariantProcessor::class)
class LogcatVarinatProcessor : VariantProcessor {

    override fun process(variant: BaseVariant) {
        if (variant !is LibraryVariant && !variant.variantData.isDynamicFeature()) {
            variant.project.dependencies.add("implementation", "${Build.GROUP}:booster-android-instrument-logcat:${Build.VERSION}")
        }
    }

}
