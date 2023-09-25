package com.didiglobal.booster.task.compression.pngquant

import com.android.build.api.variant.Variant
import com.didiglobal.booster.annotations.Priority
import com.didiglobal.booster.compression.CompressionResults
import com.didiglobal.booster.compression.generateReport
import com.didiglobal.booster.compression.isFlatPngExceptRaw
import com.didiglobal.booster.compression.isPngExceptRaw
import com.didiglobal.booster.compression.task.CompressImages
import com.didiglobal.booster.gradle.*
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

    override fun process(variant: Variant) {
        val project = variant.project
        val results = CompressionResults()
        val ignores = project.findProperty(PROPERTY_IGNORES)?.toString()?.trim()?.split(',')?.map {
            Wildcard(it)
        }?.toSet() ?: emptySet()

        Pngquant.get().newCompressionTaskCreator().createCompressionTask(variant, results, "resources", {
            variant.mergedRes.search(if (project.isAapt2Enabled) ::isFlatPngExceptRaw else ::isPngExceptRaw)
        }, ignores, variant.mergeResourcesTaskProvider).configure { task ->
            variant.project.tasks.withType(CompressImages::class.java).filter {
                it.name != task.name && it.variant.name == variant.name
            }.takeIf {
                it.isNotEmpty()
            }?.let {
                task.dependsOn(*it.toTypedArray())
            }
            task.doLast {
                results.generateReport(variant, Build.ARTIFACT)
            }
        }
    }

}

private val PROPERTY_PREFIX = Build.ARTIFACT.replace('-', '.')

private val PROPERTY_IGNORES = "$PROPERTY_PREFIX.ignores"