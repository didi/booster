package com.didiglobal.booster.task.compression

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
                    it.second.config.screenType.density
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
