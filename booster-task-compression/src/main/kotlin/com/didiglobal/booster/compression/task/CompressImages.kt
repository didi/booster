package com.didiglobal.booster.compression.task

import com.android.build.gradle.api.BaseVariant
import com.didiglobal.booster.aapt2.Aapt2Container
import com.didiglobal.booster.command.Command
import com.didiglobal.booster.command.CommandInstaller
import com.didiglobal.booster.compression.CompressionOptions
import com.didiglobal.booster.compression.CompressionResults
import com.didiglobal.booster.compression.CompressionTool
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import java.io.File

/**
 * Represents task for image compression
 *
 * @author johnsonlee
 */
abstract class CompressImages<T : CompressionOptions> : DefaultTask() {

    lateinit var variant: BaseVariant

    lateinit var tool: CompressionTool

    lateinit var results: CompressionResults

    lateinit var options: T

    lateinit var supplier: () -> Collection<File>

    val compressor: File
        get() = project.tasks.withType(CommandInstaller::class.java).find {
            it.command == tool.command
        }!!.location

    @get:Input
    val command: Command
        get() = tool.command

    @get:Input
    val variantName: String
        get() = variant.name

    @get:InputFiles
    @get:PathSensitive(PathSensitivity.RELATIVE)
    val images: Collection<File>
        get() = supplier()

}

data class ActionData(val input: File, val output: File, val cmdline: List<String>)

data class Aapt2ActionData(val input: File, val metadata: Aapt2Container.Metadata, val output: File, val cmdline: List<String>, val aapt2: List<String>)
