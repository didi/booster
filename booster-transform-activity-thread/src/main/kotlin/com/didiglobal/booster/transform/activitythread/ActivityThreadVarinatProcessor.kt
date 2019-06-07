package com.didiglobal.booster.transform.activitythread

import com.android.build.gradle.api.BaseVariant
import com.android.build.gradle.api.LibraryVariant
import com.didiglobal.booster.gradle.scope
import com.didiglobal.booster.task.spi.VariantProcessor
import com.didiglobal.booster.transform.activity.thread.Build
import com.google.auto.service.AutoService

/**
 * @author neighbWang
 */
@AutoService(VariantProcessor::class)
class ActivityThreadVarinatProcessor:VariantProcessor {

    override fun process(variant: BaseVariant) {
        if (variant !is LibraryVariant) {
            variant.scope.globalScope.project.dependencies.add("implementation", "${Build.GROUP}:booster-android-instrument-activity-thread:${Build.VERSION}")
        }
    }

}