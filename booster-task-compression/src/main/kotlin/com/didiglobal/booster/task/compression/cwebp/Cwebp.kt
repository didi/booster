package com.didiglobal.booster.task.compression.cwebp

import com.didiglobal.booster.kotlinx.touch
import com.didiglobal.booster.task.compression.CompressionTool
import com.didiglobal.booster.task.compression.SimpleCompressionTaskCreator
import com.didiglobal.booster.task.compression.cwebp.Cwebp.Companion.PROGRAM
import com.intellij.openapi.util.SystemInfo.OS_ARCH
import com.intellij.openapi.util.SystemInfo.OS_NAME
import com.intellij.openapi.util.SystemInfo.OS_VERSION
import com.intellij.openapi.util.SystemInfo.isLinux
import com.intellij.openapi.util.SystemInfo.isMac
import com.intellij.openapi.util.SystemInfo.isOsVersionAtLeast
import com.intellij.openapi.util.SystemInfo.isWindows
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

private val CWEBP = "$PROGRAM${if (isWindows) ".exe" else ""}"

private val PREBUILT_LIBWEBP_EXECUTABLE = "libwebp/" + when {
    isLinux -> "linux/" + when (OS_ARCH) {
        "x86_64" -> OS_ARCH
        else -> TODO("Unsupporeted architecture $OS_ARCH")
    }
    isMac -> "macosx/" + when {
        isOsVersionAtLeast("10.14") -> "10.14"
        isOsVersionAtLeast("10.13") -> "10.13"
        isOsVersionAtLeast("10.12") -> "10.12"
        else -> TODO("Unsupported system version $OS_VERSION")
    }
    isWindows -> "windows/" + when (OS_ARCH) {
        "x86_64", "amd64" -> OS_ARCH
        else -> TODO("Unsupported architecture $OS_ARCH")
    }
    else -> TODO("Unsupported OS $OS_NAME")
} + "/$CWEBP"
