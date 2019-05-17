package com.didiglobal.booster.task.compression

import com.android.SdkConstants.DOT_PNG
import com.android.SdkConstants.FD_RES
import com.android.SdkConstants.FD_RES_DRAWABLE
import com.android.SdkConstants.FD_RES_MIPMAP
import com.android.builder.model.AndroidProject.FD_INTERMEDIATES
import com.android.sdklib.BuildToolInfo
import com.didiglobal.booster.gradle.buildTools
import com.didiglobal.booster.gradle.project
import com.didiglobal.booster.gradle.scope
import com.didiglobal.booster.kotlinx.Quintuple
import com.didiglobal.booster.kotlinx.file
import com.didiglobal.booster.task.compression.compressor.Pngquant.Companion.EXIT_LARGER_THAN_ORIGINAL
import com.didiglobal.booster.task.compression.compressor.Pngquant.Companion.EXIT_UNSATISFIED_QUALITY
import org.gradle.api.Action
import org.gradle.api.tasks.TaskAction
import org.gradle.process.ExecSpec
import java.io.File

/**
 * Represents a task for compiled image compression
 *
 * @author johnsonlee
 */
internal open class CompressFlatImages : CompressImages() {

    @TaskAction
    override fun run() {
        val intermediates = variant.project.buildDir.file(FD_INTERMEDIATES)
        val compiledRes = intermediates.file("compiled_$FD_RES", variant.dirName, this.name)
        val compressedRes = intermediates.file("compressed_$FD_RES", variant.dirName, this.name)
        val pngquant = compressor.executable!!.absolutePath
        val aapt2 = variant.scope.buildTools.getPath(BuildToolInfo.PathId.AAPT2)

        compiledRes.mkdirs()
        compressedRes.file(FD_RES_MIPMAP).mkdirs()
        compressedRes.file(FD_RES_DRAWABLE).mkdirs()

        sources().parallelStream().map {
            it to it.metadata
        }.filter {
            it.second != null
        }.map {
            val compressed = compressedRes.file("${it.second!!.resourceName}$DOT_PNG")
            // 1 - flat file
            // 2 - compiled file metadata
            // 3 - compressed file
            // 4 - pngquant cmdline
            // 5 - aapt2 cmdline
            Quintuple(it.first, it.second!!, compressed, Action { spec: ExecSpec ->
                spec.isIgnoreExitValue = true
                spec.commandLine(pngquant, "--strip", "--skip-if-larger", "-f", "-o", compressed, "-s", "1", it.second!!.sourcePath)
            }, Action { spec: ExecSpec ->
                spec.commandLine(aapt2, "compile", "-o", it.first.parent, compressed)
            })
        }.forEach {
            val s0 = File(it.second.sourcePath).length()
            val rc = project.exec(it.fourth)
            when (rc.exitValue) {
                0 -> {
                    val s1 = it.third.length()
                    results.add(CompressionResult(it.first, s0, s1, File(it.second.sourcePath)))
                    project.exec(it.fifth)
                }
                EXIT_LARGER_THAN_ORIGINAL,
                EXIT_UNSATISFIED_QUALITY -> {
                    results.add(CompressionResult(it.first, s0, s0, File(it.second.sourcePath)))
                }
                else -> rc.rethrowFailure()
            }
        }
    }

}
