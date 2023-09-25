package com.didiglobal.booster.transform.thread

import com.android.build.api.variant.DynamicFeatureVariantBuilder
import com.android.build.api.variant.LibraryVariantBuilder
import com.android.build.api.variant.VariantBuilder
import com.didiglobal.booster.task.spi.VariantProcessor
import com.didiglobal.booster.transform.thread.Build.GROUP
import com.didiglobal.booster.transform.thread.Build.VERSION
import com.google.auto.service.AutoService
import org.gradle.api.Project

@AutoService(VariantProcessor::class)
class ThreadVariantProcessor(private val project: Project) : VariantProcessor {

    override fun beforeProcess(variantBuilder: VariantBuilder) {
        if (variantBuilder is LibraryVariantBuilder || variantBuilder is DynamicFeatureVariantBuilder) {
            return
        }
        project.dependencies.add("${variantBuilder.name}Implementation", "$GROUP:booster-android-instrument-thread:$VERSION")
    }

}
