package com.didiglobal.booster.task.compression.pngquant

import com.android.build.gradle.api.BaseVariant
import com.didiglobal.booster.annotations.Priority
import com.didiglobal.booster.compression.CompressionResults
import com.didiglobal.booster.compression.generateReport
import com.didiglobal.booster.compression.isFlatPngExceptRaw
import com.didiglobal.booster.compression.isPngExceptRaw
import com.didiglobal.booster.compression.task.CompressImages
import com.didiglobal.booster.gradle.aapt2Enabled
import com.didiglobal.booster.gradle.mergeResourcesTask
import com.didiglobal.booster.gradle.mergedRes
import com.didiglobal.booster.gradle.project
import com.didiglobal.booster.gradle.scope
import com.didiglobal.booster.kotlinx.search
import com.didiglobal.booster.task.spi.VariantProcessor
import com.google.auto.service.AutoService

/**
 * @author johnsonlee
 */
@AutoService(VariantProcessor::class)
@Priority(1)
class PngquantCompressionVariantProcessor : VariantProcessor {

    override fun process(variant: BaseVariant) {
        val results = CompressionResults()
        val filter = if (variant.project.aapt2Enabled) ::isFlatPngExceptRaw else ::isPngExceptRaw
        val cmps = variant.project.tasks.withType(CompressImages::class.java).filter {
            it.variant.name == variant.name
        }
        val deps = (cmps + variant.mergeResourcesTask).toTypedArray()
        Pngquant.get(variant)?.newCompressionTaskCreator()?.createCompressionTask(variant, results, "resources", {
            variant.scope.mergedRes.search(filter)
        }, *deps)?.doLast {
            results.generateReport(variant, Build.ARTIFACT)
        }

    }

}