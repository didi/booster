package com.didiglobal.booster.task.compression

import com.didiglobal.booster.aapt.Configuration
import com.didiglobal.booster.aapt2.Aapt2Container
import com.didiglobal.booster.aapt2.metadata
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
        val resources = sources().parallelStream().map {
            it to it.metadata
        }.collect(Collectors.toSet())

        resources.filter {
            when {
                supportsRtl -> true
                // remove ldrtl resources if RTL is not supported
                it.second.config.screenConfig.layout == Configuration.ScreenConfig.SCREEN_LAYOUT_DIR_RTL -> {
                    it.remove()
                    false
                }
                else -> true
            }
        }.groupBy({
            it.second.resourceName.substringBeforeLast('/')
        }, {
            it.first to it.second
        }).forEach { entry ->
            entry.value.groupBy({
                it.second.resourceName.substringAfterLast('/')
            }, {
                it.first to it.second
            }).map { group ->
                val highest = group.value.maxBy {
                    it.second.config.screenType.density
                }?.second?.config?.screenType?.density

                group.value.filter {
                    it.second.config.screenType.density == highest
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
