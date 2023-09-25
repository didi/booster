package com.didiglobal.booster.transform.activitythread

import com.android.build.api.variant.Variant
import com.didiglobal.booster.gradle.*
import com.didiglobal.booster.task.spi.VariantProcessor
import com.didiglobal.booster.transform.activity.thread.Build.*
import com.google.auto.service.AutoService

/**
 * @author neighbWang
 */
@AutoService(VariantProcessor::class)
class ActivityThreadVariantProcessor : VariantProcessor {

    override fun process(variant: Variant) {
        if (!variant.isLibrary && !variant.isDynamicFeature) {
            variant.project.dependencies.add("implementation", "$GROUP:booster-android-instrument-activity-thread:$VERSION")
        }
    }

}
