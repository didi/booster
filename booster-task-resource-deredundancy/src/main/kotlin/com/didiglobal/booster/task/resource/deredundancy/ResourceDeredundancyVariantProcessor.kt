package com.didiglobal.booster.task.resource.deredundancy

import com.android.build.gradle.api.BaseVariant
import com.didiglobal.booster.compression.CompressionResults
import com.didiglobal.booster.compression.generateReport
import com.didiglobal.booster.compression.isFlatPng
import com.didiglobal.booster.compression.isPng
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
 * Represents a variant processor for resource deredundancy
 *
 * @author johnsonlee
 */
@AutoService(VariantProcessor::class)
class ResourceDeredundancyVariantProcessor : VariantProcessor {

    override fun process(variant: BaseVariant) {
        val results = CompressionResults()
        val aapt2 = variant.project.aapt2Enabled
        val klassRemoveRedundantImages = if (aapt2) RemoveRedundantFlatImages::class else RemoveRedundantImages::class
        val deredundancy = variant.project.tasks.create("remove${variant.name.capitalize()}RedundantResources", klassRemoveRedundantImages.java) {
            it.outputs.upToDateWhen { false }
            it.variant = variant
            it.results = results
            it.supplier = { variant.scope.mergedRes.search(if (aapt2) ::isFlatPng else ::isPng) }
        }.dependsOn(variant.mergeResourcesTask).doLast {
            results.generateReport(variant, Build.ARTIFACT)
        }

        variant.project.tasks.withType(CompressImages::class.java).filter {
            it.variant.name == variant.name
        }.forEach {
            it.dependsOn(deredundancy)
        }
    }

}
