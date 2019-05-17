package com.didiglobal.booster.task.compression

import com.android.build.gradle.api.BaseVariant
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import java.io.File

/**
 * Represents a task for redundant resources reducing
 */
internal open class RemoveRedundantImages: DefaultTask() {

    lateinit var variant: BaseVariant

    lateinit var results: CompressionResults

    lateinit var sources: () -> Collection<File>

    @TaskAction
    open fun run() {
        TODO("Reducing redundant resources without aapt2 enabled has not supported yet")
    }

}
