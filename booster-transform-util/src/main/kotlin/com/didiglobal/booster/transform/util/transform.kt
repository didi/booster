package com.didiglobal.booster.transform.util

import com.didiglobal.booster.kotlinx.redirect
import com.didiglobal.booster.kotlinx.touch
import com.didiglobal.booster.util.search
import org.gradle.api.logging.Logging
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.InputStream
import java.io.OutputStream
import java.util.jar.JarEntry
import java.util.jar.JarFile
import java.util.jar.JarOutputStream

private val logger = Logging.getLogger("transform")

/**
 * Transform this file or directory to the output by the specified transformer
 *
 * @param output The output location
 * @param transformer The byte data transformer
 */
fun File.transform(output: File, transformer: (ByteArray) -> ByteArray = { it -> it }) {
    when {
        isDirectory -> {
            val base = this.toURI()
            this.search().forEach {
                it.transform(File(output, base.relativize(it.toURI()).path), transformer)
            }
        }
        isFile -> {
            when (output.extension.toLowerCase()) {
                "jar" -> {
                    JarOutputStream(output.touch().outputStream()).use { dest ->
                        JarFile(this).use { jar ->
                            jar.entries().asSequence().forEach { entry ->
                                dest.putNextEntry(JarEntry(entry.name))
                                if (!entry.isDirectory) {
                                    when (entry.name.substringAfterLast('.', "")) {
                                        "class" -> jar.getInputStream(entry).use { src ->
                                            logger.info("Transforming ${this.absolutePath}!/${entry.name}")
                                            src.transform(transformer).redirect(dest)
                                        }
                                        else -> jar.getInputStream(entry).use { src ->
                                            src.copyTo(dest)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                "class" -> inputStream().use {
                    logger.info("Transforming ${this.absolutePath}")
                    it.transform(transformer).redirect(output)
                }
                else -> this.copyTo(output, true)
            }
        }
        else -> TODO("Unexpected file: ${this.absolutePath}")
    }
}

fun InputStream.transform(transformer: (ByteArray) -> ByteArray): ByteArray {
    return transformer(readBytes())
}

private const val DEFAULT_BUFFER_SIZE = 8 * 1024

private fun InputStream.readBytes(estimatedSize: Int = DEFAULT_BUFFER_SIZE): ByteArray {
    val buffer = ByteArrayOutputStream(Math.max(estimatedSize, this.available()))
    copyTo(buffer)
    return buffer.toByteArray()
}

private fun InputStream.copyTo(out: OutputStream, bufferSize: Int = DEFAULT_BUFFER_SIZE): Long {
    var bytesCopied: Long = 0
    val buffer = ByteArray(bufferSize)
    var bytes = read(buffer)
    while (bytes >= 0) {
        out.write(buffer, 0, bytes)
        bytesCopied += bytes
        bytes = read(buffer)
    }
    return bytesCopied
}
