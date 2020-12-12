package com.didiglobal.booster.task.compression.pngquant

import com.android.build.gradle.api.BaseVariant
import com.didiglobal.booster.annotations.Priority
import com.didiglobal.booster.compression.CompressionResults
import com.didiglobal.booster.compression.ResourceNameFilter
import com.didiglobal.booster.compression.generateReport
import com.didiglobal.booster.compression.isFlatPngExceptRaw
import com.didiglobal.booster.compression.isPngExceptRaw
import com.didiglobal.booster.compression.task.CompressImages
import com.didiglobal.booster.gradle.aapt2Enabled
import com.didiglobal.booster.gradle.mergeResourcesTask
import com.didiglobal.booster.gradle.mergedRes
import com.didiglobal.booster.gradle.project
import com.didiglobal.booster.kotlinx.Wildcard
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
        val compress = variant.project.tasks.withType(CompressImages::class.java).filter {
            it.variant.name == variant.name
        }
        val filter: ResourceNameFilter = if (variant.project.hasProperty(PROPERTY_IGNORES)) {
            val ignores = "${variant.project.property(PROPERTY_IGNORES)}".trim().split(',').map(Wildcard.Companion::valueOf).toSet();
            { res -> ignores.none { it.matches(res) } }
        } else {
            { true }
        }
        Pngquant.get(variant)?.newCompressionTaskCreator()?.createCompressionTask(variant, results, "resources", {
            variant.mergedRes.search(if (variant.project.aapt2Enabled) ::isFlatPngExceptRaw else ::isPngExceptRaw)
        }, filter, *(compress + variant.mergeResourcesTask).toTypedArray())?.doLast {
            results.generateReport(variant, Build.ARTIFACT)
        }

    }

}

private val PROPERTY_PREFIX = Build.ARTIFACT.replace('-', '.')

private val PROPERTY_IGNORES = "$PROPERTY_PREFIX.ignores"