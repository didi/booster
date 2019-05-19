package com.didiglobal.booster.task.compression.cwebp

import com.didiglobal.booster.kotlinx.CSI_RED
import com.didiglobal.booster.kotlinx.CSI_RESET
import com.didiglobal.booster.task.compression.ActionData
import com.didiglobal.booster.task.compression.CompressImages
import com.didiglobal.booster.task.compression.CompressionResult
import org.gradle.api.tasks.TaskAction
import java.io.File

/**
 * Represents a task for image compression using cwebp
 *
 * @author johnsonlee
 */
internal open class CwebpCompressImages : CompressImages() {

    open fun compress(filter: (File) -> Boolean) {
        sources().parallelStream().filter(filter).map { input ->
            val output = File(input.absolutePath.substringBeforeLast('.') + ".webp")
            ActionData(input, output, listOf(cmdline.executable!!.absolutePath, "-mt", "-quiet", "-q", "80", "-o", output.absolutePath, input.absolutePath))
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
    open fun run() {
        compress { true }
    }

}

