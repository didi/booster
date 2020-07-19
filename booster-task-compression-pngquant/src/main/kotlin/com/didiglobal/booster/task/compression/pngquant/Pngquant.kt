package com.didiglobal.booster.task.compression.pngquant

import com.android.build.gradle.api.BaseVariant
import com.didiglobal.booster.command.CommandService
import com.didiglobal.booster.compression.CompressionTaskCreatorFactory
import com.didiglobal.booster.compression.CompressionTool
import com.didiglobal.booster.compression.SimpleCompressionTaskCreator
import com.didiglobal.booster.kotlinx.OS
import com.didiglobal.booster.task.compression.pngquant.Pngquant.Companion.PROGRAM

/**
 * Represents utility class for pngquant operation
 *
 * @author johnsonlee
 */
internal class Pngquant : CompressionTool(CommandService.get(PNGQUANT)), CompressionTaskCreatorFactory {

    override fun newCompressionTaskCreator() = SimpleCompressionTaskCreator(this) { aapt2 ->
        if (aapt2) PngquantCompressFlatImages::class else PngquantCompressImages::class
    }

    companion object {

        const val PROGRAM = "pngquant"

        fun get(@Suppress("UNUSED_PARAMETER") variant: BaseVariant): Pngquant? {
            return Pngquant()
        }

    }

}

private val PNGQUANT = "$PROGRAM${OS.executableSuffix}"

private val PROPERTY_PREFIX = Build.ARTIFACT.replace('-', '.')
internal val PROPERTY_BIN = "${PROPERTY_PREFIX}.bin"
internal val PROPERTY_OPTION_QUALITY = "${PROPERTY_PREFIX}.option.quality"
internal val PROPERTY_OPTION_SPEED = "${PROPERTY_PREFIX}.option.speed"
