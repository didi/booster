package com.didiglobal.booster.task.compression.cwebp

import com.android.build.gradle.api.BaseVariant
import com.didiglobal.booster.compression.CompressionResults
import com.didiglobal.booster.compression.generateReport
import com.didiglobal.booster.compression.isFlatPngExceptRaw
import com.didiglobal.booster.compression.isPngExceptRaw
import com.didiglobal.booster.gradle.aapt2Enabled
import com.didiglobal.booster.gradle.mergeResourcesTask
import com.didiglobal.booster.gradle.mergedRes
import com.didiglobal.booster.gradle.project
import com.didiglobal.booster.kotlinx.search
import com.didiglobal.booster.task.spi.VariantProcessor
import com.google.auto.service.AutoService

/**
 * @author johnsonlee
 */
@AutoService(VariantProcessor::class)
class CwebpCompressionVariantProcessor : VariantProcessor {

    override fun process(variant: BaseVariant) {
        val results = CompressionResults()
        val filter = if (variant.project.aapt2Enabled) ::isFlatPngExceptRaw else ::isPngExceptRaw
        Cwebp.get(variant)?.newCompressionTaskCreator()?.createCompressionTask(variant, results, "resources", {
            variant.mergedRes.search(filter)
        }, variant.mergeResourcesTask)?.doLast {
            results.generateReport(variant, Build.ARTIFACT)
        }
    }

}