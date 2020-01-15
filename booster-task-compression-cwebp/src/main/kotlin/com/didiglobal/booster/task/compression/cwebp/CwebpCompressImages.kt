package com.didiglobal.booster.task.compression.cwebp

import com.didiglobal.booster.compression.CompressionOptions
import com.didiglobal.booster.compression.CompressionResult
import com.didiglobal.booster.compression.task.ActionData
import com.didiglobal.booster.compression.task.CompressImages
import com.didiglobal.booster.gradle.getProperty
import com.didiglobal.booster.kotlinx.CSI_RED
import com.didiglobal.booster.kotlinx.CSI_RESET
import org.gradle.api.tasks.TaskAction
import java.io.File

/**
 * Represents a task for image compression using cwebp
 *
 * @author johnsonlee
 */
internal open class CwebpCompressImages : CompressImages<CompressionOptions>() {

    protected open fun compress(filter: (File) -> Boolean) {
        supplier().parallelStream().filter(filter).map { input ->
            val output = File(input.absolutePath.substringBeforeLast('.') + ".webp")
            ActionData(input, output, listOf(tool.command.executable.canonicalPath, "-mt", "-quiet", "-q", options.quality.toString(), "-o", output.absolutePath, input.absolutePath))
        }.forEach {
            val s0 = it.input.length()
            val rc = project.exec { spec ->
                spec.isIgnoreExitValue = true
                spec.commandLine = it.cmdline
            }
            when (rc.exitValue) {
                0 -> {
                    val s1 = it.output.length()
                    if (s1 > s0) {
                        results.add(CompressionResult(it.input, s0, s0, it.input))
                        it.output.delete()
                    } else {
                        results.add(CompressionResult(it.input, s0, s1, it.input))
                        it.input.delete()
                    }
                }
                else -> {
                    logger.error("${CSI_RED}Command `${it.cmdline.joinToString(" ")}` exited with non-zero value ${rc.exitValue}$CSI_RESET")
                    results.add(CompressionResult(it.input, s0, s0, it.input))
                    it.output.delete()
                }
            }
        }
    }

    @TaskAction
    fun run() {
        this.options = CompressionOptions(project.getProperty(PROPERTY_OPTION_QUALITY, 80))
        compress(File::hasNotAlpha)
    }

}

