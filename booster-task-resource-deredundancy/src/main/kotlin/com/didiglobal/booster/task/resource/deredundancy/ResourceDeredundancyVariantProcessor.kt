package com.didiglobal.booster.task.resource.deredundancy

import com.android.build.gradle.api.BaseVariant
import com.android.build.gradle.internal.tasks.factory.dependsOn
import com.didiglobal.booster.BOOSTER
import com.didiglobal.booster.annotations.Priority
import com.didiglobal.booster.compression.CompressionResults
import com.didiglobal.booster.compression.generateReport
import com.didiglobal.booster.compression.task.CompressImages
import com.didiglobal.booster.gradle.*
import com.didiglobal.booster.task.spi.VariantProcessor
import com.google.auto.service.AutoService
import org.gradle.api.UnknownTaskException
import java.io.File


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
        val mapSourceSetPaths = project.tasks.findByName(variant.getTaskName("map", "SourceSetPaths"))

        @Suppress("DEPRECATION")
        val deredundancy = variant.project.tasks.register("remove${variant.name.capitalize()}RedundantResources", RemoveRedundantImages::class.java) { task ->
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
            mapSourceSetPaths?.let {
                dependsOn(it)
            }
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
        variant.processResTaskProvider?.dependsOn(deredundancy)
    }

    private fun BaseVariant.cachedMap(): Map<String, String> {
        return this.allArtifacts["SOURCE_SET_PATH_MAP"]
                ?.flatMap(File::readLines)
                ?.map {
                    it.split(" ")
                }?.associate {
                    it[0] to it[1]
                } ?: emptyMap()
    }
}
