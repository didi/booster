package com.didiglobal.booster.task.resource.deredundancy

import com.didiglobal.booster.aapt.Configuration
import com.didiglobal.booster.aapt2.Aapt2Container
import com.didiglobal.booster.aapt2.metadata
import com.didiglobal.booster.compression.CompressionResult
import org.gradle.api.tasks.TaskAction
import java.io.File
import java.io.IOException
import java.util.stream.Collectors

/**
 * Represents a task for redundant resources reducing
 *
 * @author johnsonlee
 */
internal open class RemoveRedundantFlatImages : RemoveRedundantImages() {

    @TaskAction
    override fun run() {
        val resources = supplier().parallelStream().map {
            it to it.metadata
        }.collect(Collectors.toSet())

        resources.filter {
            when {
                supportsRtl -> true
                // remove ldrtl resources if RTL is not supported
                it.second.configuration.screenConfig.layout == Configuration.ScreenConfig.SCREEN_LAYOUT_DIR_RTL -> {
                    it.remove()
                    false
                }
                else -> true
            }
        }.groupBy({
            it.second.resourceName.substringBeforeLast('/') // group by resource type, eg. drawable, mipmap, etc.
        }, {
            it.first to it.second
        }).forEach { entry -> // resource type => resources
            entry.value.groupBy({
                // resource name with configuration except density
                Configuration(it.second.configuration).apply {
                    screenType.density = Configuration.ScreenType.DENSITY_DEFAULT
                }.toString(it.second.resourceName.substringAfterLast('/'))
            }, {
                it.first to it.second
            }).map { group -> // resource name with configuration => resources
                // calculate the maximum density of the resource group with same name
                val highest = group.value.maxBy {
                    it.second.configuration.screenType.density
                }?.second?.configuration?.screenType?.density

                // select the grouped resources except the one with maximum density
                group.value.filter {
                    it.second.configuration.screenType.density != highest
                }
            }.flatten().parallelStream().forEach {
                it.remove()
            }
        }
    }

    private fun Pair<File, Aapt2Container.Metadata>.remove() {
        try {
            if (this.first.delete()) {
                val original = File(this.second.sourcePath)
                results.add(CompressionResult(this.first, original.length(), 0, original))
            } else {
                logger.error("Cannot delete file `${this.first}`")
            }
        } catch (e: IOException) {
            logger.error("Cannot delete file `${this.first}`", e)
        }
    }

}
