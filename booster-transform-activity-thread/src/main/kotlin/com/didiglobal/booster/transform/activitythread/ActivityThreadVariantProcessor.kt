package com.didiglobal.booster.transform.activitythread

import com.android.build.api.variant.DynamicFeatureVariantBuilder
import com.android.build.api.variant.LibraryVariantBuilder
import com.android.build.api.variant.VariantBuilder
import com.didiglobal.booster.gradle.*
import com.didiglobal.booster.task.spi.VariantProcessor
import com.didiglobal.booster.transform.activity.thread.Build.GROUP
import com.didiglobal.booster.transform.activity.thread.Build.VERSION
import com.google.auto.service.AutoService
import org.gradle.api.Project

/**
 * @author neighbWang
 */
@AutoService(VariantProcessor::class)
class ActivityThreadVariantProcessor(private val project: Project) : VariantProcessor {

    override fun beforeProcess(variantBuilder: VariantBuilder) {
        if (variantBuilder is LibraryVariantBuilder || variantBuilder is DynamicFeatureVariantBuilder) {
            return
        }
        project.dependencies.add("${variantBuilder.name}Implementation", "$GROUP:booster-android-instrument-activity-thread:$VERSION")
    }

}
