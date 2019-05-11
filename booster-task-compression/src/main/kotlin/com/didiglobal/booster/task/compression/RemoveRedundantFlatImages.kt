package com.didiglobal.booster.task.compression

import org.gradle.api.tasks.TaskAction
import java.io.File
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
            it.second != null
        }.groupBy({
            it.second!!.resourceName.substringAfterLast('/')
        }, {
            it.first to it.second!!
        }).map { group ->
            group.value.sortedByDescending {
                it.second.config.density
            }.takeLast(group.value.size - 1)
        }.flatten().parallelStream().forEach {
            if (it.first.delete()) {
                results.add(Triple(it.first, File(it.second.sourcePath).length(), 0))
            }
        }
    }

}
