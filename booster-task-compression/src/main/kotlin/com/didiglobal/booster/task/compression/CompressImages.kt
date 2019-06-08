package com.didiglobal.booster.task.compression

import com.android.build.gradle.api.BaseVariant
import com.didiglobal.booster.aapt2.ResourcesInternal
import org.gradle.api.DefaultTask
import java.io.File

/**
 * Represents task for image compression
 *
 * @author johnsonlee
 */
abstract class CompressImages : DefaultTask() {

    lateinit var variant: BaseVariant

    lateinit var cmdline: CompressionTool

    lateinit var sources: () -> Collection<File>

    lateinit var results: CompressionResults

}

internal data class ActionData(val input: File, val output: File, val cmdline: List<String>)

internal data class Aapt2ActionData(val input: File, val metadata: ResourcesInternal.CompiledFile, val output: File, val cmdline: List<String>, val aapt2: List<String>)
