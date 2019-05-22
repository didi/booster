package com.didiglobal.booster.task.compression.cwebp

import com.didiglobal.booster.kotlinx.OS
import com.didiglobal.booster.kotlinx.touch
import com.didiglobal.booster.task.compression.CompressionTool
import com.didiglobal.booster.task.compression.SimpleCompressionTaskCreator
import com.didiglobal.booster.task.compression.cwebp.Cwebp.Companion.PROGRAM
import java.io.File

/**
 * Represents utility class for webp compression
 *
 * @author johnsonlee
 */
internal class Cwebp internal constructor(path: String? = null, private val opaque: Boolean = false) : CompressionTool(CWEBP, path) {

    override fun install(location: File): Boolean {
        location.touch().outputStream().buffered().use { output ->
            javaClass.classLoader.getResourceAsStream(PREBUILT_LIBWEBP_EXECUTABLE).buffered().use { input ->
                input.copyTo(output)
            }
        }
        return true
    }

    override fun newCompressionTaskCreator() = SimpleCompressionTaskCreator(this) { aapt2 ->
        when (aapt2) {
            true -> when (opaque) {
                true -> CwebpCompressOpaqueFlatImages::class
                else -> CwebpCompressFlatImages::class
            }
            else -> when (opaque) {
                true -> CwebpCompressOpaqueImages::class
                else -> CwebpCompressImages::class
            }
        }
    }

    companion object {

        const val PROGRAM = "cwebp"

    }

}

private val CWEBP = "$PROGRAM${OS.executableSuffix}"

private val PREBUILT_LIBWEBP_EXECUTABLE = "libwebp/" + when {
    OS.isLinux() -> "linux/" + when (OS.arch) {
        "x64", "x86_64", "amd64" -> "x64"
        else -> TODO("Unsupported architecture ${OS.arch}")
    }
    OS.isMac() -> "macosx/" + when {
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
