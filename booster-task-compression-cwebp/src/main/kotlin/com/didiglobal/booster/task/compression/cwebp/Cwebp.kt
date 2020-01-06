package com.didiglobal.booster.task.compression.cwebp

import com.android.SdkConstants
import com.android.build.gradle.api.BaseVariant
import com.didiglobal.booster.compression.CompressionOptions
import com.didiglobal.booster.compression.CompressionTool
import com.didiglobal.booster.compression.SimpleCompressionTaskCreator
import com.didiglobal.booster.gradle.getProperty
import com.didiglobal.booster.gradle.project
import com.didiglobal.booster.gradle.scope
import com.didiglobal.booster.kotlinx.OS
import com.didiglobal.booster.kotlinx.file
import com.didiglobal.booster.kotlinx.touch
import com.didiglobal.booster.task.compression.cwebp.Cwebp.Companion.PROGRAM
import java.io.File

/**
 * Represents utility class for webp compression
 *
 * @author johnsonlee
 *
 * @property path The path of executable
 * @property opaque The value indicates if compression supports transparent images
 */
class Cwebp internal constructor(path: String, val supportAlpha: Boolean) : CompressionTool(CWEBP, path) {

    override fun install(location: File): Boolean {
        location.touch().outputStream().buffered().use { output ->
            javaClass.classLoader.getResourceAsStream(PREBUILT_LIBWEBP_EXECUTABLE)!!.buffered().use { input ->
                input.copyTo(output)
            }
        }
        return true
    }

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

        fun get(variant: BaseVariant): Cwebp? {
            val binDir = variant.project.buildDir.file(SdkConstants.FD_OUTPUT).absolutePath
            val minSdkVersion = variant.scope.minSdkVersion.apiLevel
            // https://developer.android.com/studio/write/convert-webp
            return when {
                minSdkVersion >= 18 -> Cwebp(binDir, true)
                minSdkVersion in 14..17 -> Cwebp(binDir, false)
                else -> null
            }
        }

    }

}

private val CWEBP = "$PROGRAM${OS.executableSuffix}"

private val PREBUILT_LIBWEBP_EXECUTABLE = "libwebp/" + when {
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
