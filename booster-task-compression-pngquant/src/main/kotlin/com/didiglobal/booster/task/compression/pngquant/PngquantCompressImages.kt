package com.didiglobal.booster.task.compression.pngquant

import com.android.SdkConstants.DOT_PNG
import com.didiglobal.booster.compression.CompressionResult
import com.didiglobal.booster.compression.task.ActionData
import com.didiglobal.booster.compression.task.CompressImages
import com.didiglobal.booster.gradle.getProperty
import com.didiglobal.booster.kotlinx.CSI_RED
import com.didiglobal.booster.kotlinx.CSI_RESET
import org.gradle.api.tasks.TaskAction

/**
 * Represents a task for image compression using pngquant
 *
 * @author johnsonlee
 */
internal open class PngquantCompressImages : CompressImages<PngquantOptions>() {

    @TaskAction
    fun run() {
        this.options = PngquantOptions(
                project.getProperty(PROPERTY_OPTION_QUALITY, 80),
                project.getProperty(PROPERTY_OPTION_SPEED, 3)
        )
        compress()
    }

    protected open fun compress() {
        supplier().map {
            ActionData(it, it, listOf(tool.command.executable.canonicalPath, "--strip", "--skip-if-larger", "-f", "--ext", DOT_PNG, "-s", "${options.speed}", "-Q", "${options.quality}-100", it.absolutePath))
        }.parallelStream().forEach {
            val s0 = it.input.length()
            val rc = project.exec { spec ->
                spec.isIgnoreExitValue = true
                spec.commandLine = it.cmdline
            }
            when (rc.exitValue) {
                0 -> {
                    val s1 = it.input.length()
                    results.add(CompressionResult(it.input, s0, s1, it.input))
                }
                else -> {
                    logger.error("${CSI_RED}Command `${it.cmdline.joinToString(" ")}` exited with non-zero value ${rc.exitValue}$CSI_RESET")
                    results.add(CompressionResult(it.input, s0, s0, it.input))
                }
            }
        }
    }

}
