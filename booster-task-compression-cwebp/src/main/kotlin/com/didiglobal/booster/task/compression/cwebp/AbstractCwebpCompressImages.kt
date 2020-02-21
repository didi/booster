package com.didiglobal.booster.task.compression.cwebp

import com.didiglobal.booster.compression.CompressionOptions
import com.didiglobal.booster.compression.task.CompressImages
import com.didiglobal.booster.gradle.getProperty
import org.gradle.api.tasks.TaskAction
import java.io.File

/**
 * Represents an abstraction of cwebp compression task
 *
 * @author johnsonlee
 */
abstract class AbstractCwebpCompressImages : CompressImages<CompressionOptions>() {

    @TaskAction
    fun run() {
        this.options = CompressionOptions(project.getProperty(PROPERTY_OPTION_QUALITY, 80))
        compress(File::hasNotAlpha)
    }

    protected abstract fun compress(filter: (File) -> Boolean)

}