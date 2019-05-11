package com.didiglobal.booster.task.compression

import android.aapt.pb.internal.ResourcesInternal
import com.android.build.gradle.api.BaseVariant
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import java.io.File

/**
 * Represents a task for redundant resources reducing
 *
 * @author johnsonlee
 */
internal open class ReduceRedundancy : DefaultTask() {

    lateinit var variant: BaseVariant

    lateinit var results: CompressionResult

    lateinit var sources: () -> Collection<File>

    lateinit var retained: () -> Collection<Pair<File, ResourcesInternal.CompiledFile?>>

    @TaskAction
    fun run() {
        val resources = sources().asSequence().map {
            it to it.metadata
        }

        resources.filterNot {
            it.second == null
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

        this.retained = {
            resources.filter {
                it.first.exists()
            }.toList()
        }
    }

}
