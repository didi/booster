package com.didiglobal.booster.task.compression.cwebp

import com.android.build.gradle.api.BaseVariant
import com.didiglobal.booster.command.CommandService
import com.didiglobal.booster.compression.CompressionTool
import com.didiglobal.booster.compression.SimpleCompressionTaskCreator
import com.didiglobal.booster.gradle.scope
import com.didiglobal.booster.kotlinx.OS
import com.didiglobal.booster.task.compression.cwebp.Cwebp.Companion.PROGRAM

/**
 * Represents utility class for webp compression
 *
 * @author johnsonlee
 *
 * @property opaque The value indicates if compression supports transparent images
 */
class Cwebp internal constructor(val supportAlpha: Boolean) : CompressionTool(CommandService.get(CWEBP)) {

    override fun newCompressionTaskCreator() = SimpleCompressionTaskCreator(this) { aapt2 ->
        when (aapt2) {
            true -> when (supportAlpha) {
                true -> CwebpCompressOpaqueFlatImages::class
                else -> CwebpCompressFlatImages::class
            }
            else -> when (supportAlpha) {
                true -> CwebpCompressOpaqueImages::class
                else -> CwebpCompressImages::class
            }
        }
    }

    companion object {

        const val PROGRAM = "cwebp"

        /**
         * @see <a href="https://developer.android.com/studio/write/convert-webp">convert-webp</a>
         */
        fun get(variant: BaseVariant): Cwebp? {
            val minSdkVersion = variant.scope.minSdkVersion.apiLevel
            return when {
                minSdkVersion >= 18 -> Cwebp(true)
                minSdkVersion in 14..17 -> Cwebp(false)
                else -> null
            }
        }

    }

}

internal val CWEBP = "$PROGRAM${OS.executableSuffix}"

internal val PREBUILT_CWEBP_EXECUTABLE = "bin/" + when {
    OS.isLinux() -> "linux/" + when (OS.arch) {
        "x64", "x86_64", "amd64" -> "x64"
        else -> TODO("Unsupported architecture ${OS.arch}")
    }
    OS.isMac() -> "macosx/" + when {
        OS.version >= "10.15" -> "10.15"
        OS.version >= "10.14" -> "10.14"
        OS.version >= "10.13" -> "10.13"
        OS.version >= "10.12" -> "10.12"
        else -> TODO("Unsupported system version ${OS.version}")
    }
    OS.isWindows() -> "windows/" + when (OS.arch) {
        "x64", "x86_64", "amd64" -> "x64"
        else -> TODO("Unsupported architecture ${OS.arch}")
    }
    else -> TODO("Unsupported OS ${OS.name}")
} + "/$CWEBP"

private val PROPERTY_PREFIX = Build.ARTIFACT.replace('-', '.')
internal val PROPERTY_OPTION_QUALITY = "${PROPERTY_PREFIX}.option.quality"
