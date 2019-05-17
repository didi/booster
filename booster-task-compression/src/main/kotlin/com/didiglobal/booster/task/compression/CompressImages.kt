package com.didiglobal.booster.task.compression

import com.android.SdkConstants.DOT_PNG
import com.android.build.gradle.api.BaseVariant
import com.didiglobal.booster.task.compression.compressor.ImageCompressor
import com.didiglobal.booster.task.compression.compressor.Pngquant.Companion.EXIT_LARGER_THAN_ORIGINAL
import com.didiglobal.booster.task.compression.compressor.Pngquant.Companion.EXIT_UNSATISFIED_QUALITY
import org.gradle.api.Action
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import org.gradle.process.ExecSpec
import java.io.File

/**
 * Represents a task for image compression
 *
 * @author johnsonlee
 */
internal open class CompressImages : DefaultTask() {

    lateinit var variant: BaseVariant

    lateinit var sources: () -> Collection<File>

    lateinit var results: CompressionResults

    lateinit var compressor: ImageCompressor

    @TaskAction
    open fun run() {
        val pngquant = compressor.executable!!.absolutePath

        sources().map { png ->
            png to Action { spec: ExecSpec ->
                spec.isIgnoreExitValue = true
                spec.commandLine(pngquant, "--strip", "--skip-if-larger", "-f", "--ext", DOT_PNG, "-s", "1", png.absolutePath)
            }
        }.parallelStream().forEach {
            val s0 = it.first.length()
            val rc = project.exec(it.second)
            when (rc.exitValue) {
                0 -> {
                    val s1 = it.first.length()
                    results.add(CompressionResult(it.first, s0, s1, it.first))
                }
                EXIT_LARGER_THAN_ORIGINAL,
                EXIT_UNSATISFIED_QUALITY -> {
                    results.add(CompressionResult(it.first, s0, s0, it.first))
                }
                else -> rc.rethrowFailure()
            }
        }
    }

}
