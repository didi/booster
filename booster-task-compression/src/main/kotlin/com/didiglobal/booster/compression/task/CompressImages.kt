package com.didiglobal.booster.compression.task

import com.android.build.gradle.api.BaseVariant
import com.didiglobal.booster.aapt2.Aapt2Container
import com.didiglobal.booster.compression.CompressionOptions
import com.didiglobal.booster.compression.CompressionResults
import com.didiglobal.booster.compression.CompressionTool
import org.gradle.api.DefaultTask
import java.io.File

/**
 * Represents task for image compression
 *
 * @author johnsonlee
 */
abstract class CompressImages<T: CompressionOptions> : DefaultTask() {

    lateinit var variant: BaseVariant

    lateinit var cmdline: CompressionTool

    lateinit var supplier: () -> Collection<File>

    lateinit var results: CompressionResults

    lateinit var options: T

}

data class ActionData(val input: File, val output: File, val cmdline: List<String>)

data class Aapt2ActionData(val input: File, val metadata: Aapt2Container.Metadata, val output: File, val cmdline: List<String>, val aapt2: List<String>)
