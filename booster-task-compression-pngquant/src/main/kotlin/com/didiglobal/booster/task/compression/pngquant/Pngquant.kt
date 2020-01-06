package com.didiglobal.booster.task.compression.pngquant

import com.android.build.gradle.api.BaseVariant
import com.didiglobal.booster.compression.CompressionTool
import com.didiglobal.booster.compression.SimpleCompressionTaskCreator
import com.didiglobal.booster.gradle.getProperty
import com.didiglobal.booster.gradle.project
import com.didiglobal.booster.kotlinx.OS
import com.didiglobal.booster.task.compression.pngquant.Pngquant.Companion.PROGRAM
import java.io.File

/**
 * Represents utility class for pngquant operation
 *
 * @author johnsonlee
 */
internal class Pngquant internal constructor(path: String) : CompressionTool(PNGQUANT, path) {

    override fun install(location: File) = this.isInstalled

    override fun newCompressionTaskCreator() = SimpleCompressionTaskCreator(this) { aapt2 ->
        if (aapt2) PngquantCompressFlatImages::class else PngquantCompressImages::class
    }

    companion object {

        const val PROGRAM = "pngquant"

        fun get(variant: BaseVariant): Pngquant? {
            val quality = variant.project.getProperty(PROPERTY_OPTION_QUALITY, 80)
            val speed = variant.project.getProperty(PROPERTY_OPTION_SPEED, 3)

            return Pngquant(variant.project.getProperty(PROPERTY_BIN, "")).let {
                if (it.isInstalled) it else null
            }
        }

    }

}

private val PNGQUANT = "$PROGRAM${OS.executableSuffix}"

private val PROPERTY_PREFIX = Build.ARTIFACT.replace('-', '.')
internal val PROPERTY_BIN = "${PROPERTY_PREFIX}.bin"
internal val PROPERTY_OPTION_QUALITY = "${PROPERTY_PREFIX}.option.quality"
internal val PROPERTY_OPTION_SPEED = "${PROPERTY_PREFIX}.option.speed"
