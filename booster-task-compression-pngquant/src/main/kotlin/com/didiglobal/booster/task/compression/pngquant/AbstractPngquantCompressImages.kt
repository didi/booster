package com.didiglobal.booster.task.compression.pngquant

import com.didiglobal.booster.compression.task.CompressImages
import com.didiglobal.booster.gradle.getProperty
import org.gradle.api.tasks.TaskAction

/**
 * Represents an abstraction of pngquant compression task
 * @author johnsonlee
 */
abstract class AbstractPngquantCompressImages: CompressImages<PngquantOptions>() {

    @TaskAction
    fun run() {
        this.options = PngquantOptions(
                project.getProperty(PROPERTY_OPTION_QUALITY, 80),
                project.getProperty(PROPERTY_OPTION_SPEED, 3)
        )
        compress()
    }

    protected abstract fun compress()

}