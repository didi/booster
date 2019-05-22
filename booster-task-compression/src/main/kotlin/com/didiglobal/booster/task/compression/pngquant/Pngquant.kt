package com.didiglobal.booster.task.compression.pngquant

import com.didiglobal.booster.kotlinx.OS
import com.didiglobal.booster.task.compression.CompressionTool
import com.didiglobal.booster.task.compression.SimpleCompressionTaskCreator
import com.didiglobal.booster.task.compression.pngquant.Pngquant.Companion.PROGRAM
import java.io.File

/**
 * Represents utility class for pngquant operation
 *
 * @author johnsonlee
 */
internal class Pngquant internal constructor(path: String? = null) : CompressionTool(PNGQUANT, path) {

    override fun install(location: File) = this.isInstalled

    override fun newCompressionTaskCreator() = SimpleCompressionTaskCreator(this) { aapt2 ->
        if (aapt2) PngquantCompressFlatImages::class else PngquantCompressImages::class
    }

    companion object {

        const val PROGRAM = "pngquant"

    }

}

private val PNGQUANT = "$PROGRAM${OS.executableSuffix}"
