package com.didiglobal.booster.task.compression.pngquant

import com.android.SdkConstants.DOT_PNG
import com.android.SdkConstants.FD_RES
import com.android.SdkConstants.FD_RES_DRAWABLE
import com.android.SdkConstants.FD_RES_MIPMAP
import com.android.builder.model.AndroidProject.FD_INTERMEDIATES
import com.android.sdklib.BuildToolInfo
import com.didiglobal.booster.aapt2.metadata
import com.didiglobal.booster.gradle.buildTools
import com.didiglobal.booster.gradle.project
import com.didiglobal.booster.gradle.scope
import com.didiglobal.booster.kotlinx.CSI_RED
import com.didiglobal.booster.kotlinx.CSI_RESET
import com.didiglobal.booster.kotlinx.file
import com.didiglobal.booster.task.compression.Aapt2ActionData
import com.didiglobal.booster.task.compression.CompressionResult
import org.gradle.api.tasks.TaskAction
import java.io.File

/**
 * Represents a task for compiled image compression using pngquant
 *
 * @author johnsonlee
 */
internal open class PngquantCompressFlatImages : PngquantCompressImages() {

    @TaskAction
    override fun run() {
        val intermediates = variant.project.buildDir.file(FD_INTERMEDIATES)
        val compiledRes = intermediates.file("compiled_${FD_RES}_pngquant", variant.dirName, this.name)
        val compressedRes = intermediates.file("compressed_${FD_RES}_pngquant", variant.dirName, this.name)
        val pngquant = cmdline.executable!!.absolutePath
        val aapt2 = variant.scope.buildTools.getPath(BuildToolInfo.PathId.AAPT2)

        compiledRes.mkdirs()
        compressedRes.file(FD_RES_MIPMAP).mkdirs()
        compressedRes.file(FD_RES_DRAWABLE).mkdirs()

        sources().parallelStream().map {
            it to it.metadata
        }.map {
            val output = compressedRes.file("${it.second.resourcePath.substringBeforeLast('.')}$DOT_PNG")
            Aapt2ActionData(it.first, it.second, output,
                    listOf(pngquant, "--strip", "--skip-if-larger", "-f", "-o", output.absolutePath, "-s", "1", it.second.sourcePath),
                    listOf(aapt2, "compile", "-o", it.first.parent, output.absolutePath))
        }.forEach {
            it.output.parentFile.mkdirs()
            val s0 = File(it.metadata.sourcePath).length()
            val rc = project.exec { spec ->
                spec.isIgnoreExitValue = true
                spec.commandLine = it.cmdline
            }
            when (rc.exitValue) {
                0 -> {
                    val s1 = it.output.length()
                    val rcAapt2 = project.exec { spec ->
                        spec.isIgnoreExitValue = true
                        spec.commandLine = it.aapt2
                    }

                    if (0 == rcAapt2.exitValue) {
                        results.add(CompressionResult(it.input, s0, s1, File(it.metadata.sourcePath)))
                    } else {
                        logger.error("${CSI_RED}Command `${it.aapt2.joinToString(" ")}` exited with non-zero value ${rc.exitValue}$CSI_RESET")
                        results.add(CompressionResult(it.input, s0, s0, File(it.metadata.sourcePath)))
                        rcAapt2.assertNormalExitValue()
                    }
                }
                else -> {
                    logger.error("${CSI_RED}Command `${it.cmdline.joinToString(" ")}` exited with non-zero value ${rc.exitValue}$CSI_RESET")
                    results.add(CompressionResult(it.input, s0, s0, File(it.metadata.sourcePath)))
                }
            }
        }
    }

}
