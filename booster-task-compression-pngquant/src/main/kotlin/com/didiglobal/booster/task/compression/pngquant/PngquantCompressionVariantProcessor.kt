package com.didiglobal.booster.task.compression.pngquant

import com.android.build.gradle.api.BaseVariant
import com.didiglobal.booster.compression.CompressionResults
import com.didiglobal.booster.compression.generateReport
import com.didiglobal.booster.compression.isFlatPng
import com.didiglobal.booster.compression.isPng
import com.didiglobal.booster.gradle.aapt2Enabled
import com.didiglobal.booster.gradle.mergeResourcesTask
import com.didiglobal.booster.gradle.mergedRes
import com.didiglobal.booster.gradle.processResTask
import com.didiglobal.booster.gradle.project
import com.didiglobal.booster.gradle.scope
import com.didiglobal.booster.task.spi.VariantProcessor
import com.didiglobal.booster.util.search

class PngquantCompressionVariantProcessor : VariantProcessor {

    override fun process(variant: BaseVariant) {
        val results = CompressionResults()
        val filter = if (variant.project.aapt2Enabled) ::isFlatPng else ::isPng

        variant.processResTask.doLast {
            results.generateReport(variant, Build.ARTIFACT)
        }

        Pngquant.get(variant)?.newCompressionTaskCreator()?.createCompressionTask(variant, results, "resources", {
            variant.scope.mergedRes.search(filter)
        }, variant.mergeResourcesTask)
    }

}