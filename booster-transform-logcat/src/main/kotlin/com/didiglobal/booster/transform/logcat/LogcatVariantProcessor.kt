package com.didiglobal.booster.transform.logcat

import com.android.build.api.variant.Variant
import com.didiglobal.booster.gradle.isDynamicFeature
import com.didiglobal.booster.gradle.isLibrary
import com.didiglobal.booster.gradle.project
import com.didiglobal.booster.task.spi.VariantProcessor
import com.didiglobal.booster.transform.logcat.Build.*
import com.google.auto.service.AutoService

/**
 * @author neighbWang
 */
@AutoService(VariantProcessor::class)
class LogcatVariantProcessor : VariantProcessor {

    override fun process(variant: Variant) {
        if (!variant.isLibrary && !variant.isDynamicFeature) {
            variant.project.dependencies.add("implementation", "$GROUP:booster-android-instrument-logcat:$VERSION")
        }
    }

}
