package com.didiglobal.booster.transform.logcat

import com.android.build.api.variant.DynamicFeatureVariant
import com.android.build.api.variant.LibraryVariant
import com.android.build.api.variant.Variant
import com.didiglobal.booster.task.spi.VariantProcessor
import com.didiglobal.booster.transform.logcat.Build.GROUP
import com.didiglobal.booster.transform.logcat.Build.VERSION
import com.google.auto.service.AutoService
import org.gradle.api.Project

/**
 * @author neighbWang
 */
@AutoService(VariantProcessor::class)
class LogcatVariantProcessor(private val project: Project) : VariantProcessor {

    override fun process(variant: Variant) {
        super.process(variant)
        if (variant is LibraryVariant || variant is DynamicFeatureVariant) {
            return
        }
        project.dependencies.add("${variant.name}Implementation", "$GROUP:booster-android-instrument-logcat:$VERSION")
    }

}
