package com.didiglobal.booster.task.resource.deredundancy

import com.android.build.gradle.api.BaseVariant
import com.android.build.gradle.internal.tasks.factory.dependsOn
import com.didiglobal.booster.BOOSTER
import com.didiglobal.booster.annotations.Priority
import com.didiglobal.booster.compression.CompressionResults
import com.didiglobal.booster.compression.generateReport
import com.didiglobal.booster.compression.task.CompressImages
import com.didiglobal.booster.gradle.getTaskName
import com.didiglobal.booster.gradle.isAapt2Enabled
import com.didiglobal.booster.gradle.mergeResourcesTaskProvider
import com.didiglobal.booster.gradle.project
import com.didiglobal.booster.task.spi.VariantProcessor
import com.google.auto.service.AutoService
import org.gradle.api.UnknownTaskException


/**
 * Represents a variant processor for resource deredundancy
 *
 * @author johnsonlee
 */
@Priority(-1)
@AutoService(VariantProcessor::class)
class ResourceDeredundancyVariantProcessor : VariantProcessor {

    override fun process(variant: BaseVariant) {
        val project = variant.project
        val results = CompressionResults()
        val klassRemoveRedundantImages = if (project.isAapt2Enabled) RemoveRedundantFlatImages::class else RemoveRedundantImages::class
        @Suppress("DEPRECATION")
        val deredundancy = variant.project.tasks.register("remove${variant.name.capitalize()}RedundantResources", klassRemoveRedundantImages.java) { task ->
            task.group = BOOSTER
            task.description = "Remove redundant resources for ${variant.name}"
            task.outputs.upToDateWhen { false }
            task.variant = variant
            task.results = results
        }.apply {
            (try {
                project.tasks.named(variant.getTaskName("process", "Manifest"))
            } catch (e: UnknownTaskException) {
                null
            })?.let {
                dependsOn(it)
            }
            dependsOn(variant.mergeResourcesTaskProvider)
            configure {
                it.doLast {
                    results.generateReport(variant, Build.ARTIFACT)
                }
            }
        }

        variant.project.tasks.withType(CompressImages::class.java).filter {
            it.variant.name == variant.name
        }.forEach {
            it.dependsOn(deredundancy)
        }
    }

}
