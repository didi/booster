package com.didiglobal.booster.task.compression

import com.didiglobal.booster.aapt2.metadata
import com.didiglobal.booster.gradle.GTE_V3_2
import com.didiglobal.booster.kotlinx.CSI_RESET
import com.didiglobal.booster.kotlinx.CSI_YELLOW
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
        when {
            GTE_V3_2 -> removeFlatImages()
            else -> logger.warn("${CSI_YELLOW}Removing legacy flat images is not supported yet$CSI_RESET")

        }
    }

    private fun removeFlatImages() {
        val resources = sources().parallelStream().map {
            it to it.metadata
        }.collect(Collectors.toSet())

        resources.groupBy({
            it.second.resourceName.substringBeforeLast('/')
        }, {
            it.first to it.second
        }).forEach { entry ->
            entry.value.groupBy({
                it.second.resourceName.substringAfterLast('/')
            }, {
                it.first to it.second
            }).map { group ->
                group.value.sortedByDescending {
                    it.second.config.density
                }.takeLast(group.value.size - 1)
            }.flatten().parallelStream().forEach {
                try {
                    if (it.first.delete()) {
                        val original = File(it.second.sourcePath)
                        results.add(CompressionResult(it.first, original.length(), 0, original))
                    } else {
                        logger.error("Cannot delete file `${it.first}`")
                    }
                } catch (e: IOException) {
                    logger.error("Cannot delete file `${it.first}`", e)
                }
            }
        }
    }

}
