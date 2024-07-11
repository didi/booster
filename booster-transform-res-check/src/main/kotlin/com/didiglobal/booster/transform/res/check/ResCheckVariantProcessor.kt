package com.didiglobal.booster.transform.res.check

import com.android.build.api.variant.DynamicFeatureVariant
import com.android.build.api.variant.LibraryVariant
import com.android.build.api.variant.Variant
import com.didiglobal.booster.task.spi.VariantProcessor
import com.didiglobal.booster.transform.res.check.Build.GROUP
import com.didiglobal.booster.transform.res.check.Build.VERSION
import com.google.auto.service.AutoService
import org.gradle.api.Project

/**
 * @author neighbWang
 */
@AutoService(VariantProcessor::class)
class ResCheckVariantProcessor(private val project: Project) : VariantProcessor {

    override fun process(variant: Variant) {
        super.process(variant)
        if (variant is LibraryVariant || variant is DynamicFeatureVariant) {
            return
        }
        project.dependencies.add("${variant.name}Implementation", "$GROUP:booster-android-instrument-res-check:$VERSION")
    }

}
