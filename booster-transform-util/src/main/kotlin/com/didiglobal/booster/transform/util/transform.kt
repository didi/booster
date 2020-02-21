package com.didiglobal.booster.transform.util

import com.didiglobal.booster.kotlinx.NCPU
import com.didiglobal.booster.kotlinx.redirect
import com.didiglobal.booster.kotlinx.touch
import com.didiglobal.booster.util.search
import org.apache.commons.compress.archivers.jar.JarArchiveEntry
import org.apache.commons.compress.archivers.zip.ParallelScatterZipCreator
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream
import org.apache.commons.compress.parallel.InputStreamSupplier
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.InputStream
import java.io.OutputStream
import java.util.concurrent.Executors
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.RejectedExecutionHandler
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit
import java.util.jar.JarFile
import java.util.zip.ZipEntry
import java.util.zip.ZipFile
import java.util.zip.ZipInputStream

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
            this.search().parallelStream().forEach {
                it.transform(File(output, base.relativize(it.toURI()).path), transformer)
            }
        }
        isFile -> {
            when (output.extension.toLowerCase()) {
                "jar" -> JarFile(this).use {
                    it.transform(output, ::JarArchiveEntry, transformer)
                }
                "class" -> inputStream().use {
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

fun ZipFile.transform(output: File, entryFactory: (ZipEntry) -> ZipArchiveEntry = ::ZipArchiveEntry, transformer: (ByteArray) -> ByteArray = { it -> it }) {
    val creator = ParallelScatterZipCreator(ThreadPoolExecutor(NCPU, NCPU, 0L, TimeUnit.MILLISECONDS, LinkedBlockingQueue<Runnable>(), Executors.defaultThreadFactory(), RejectedExecutionHandler { runnable, _ ->
        runnable.run()
    }))
    val entries = mutableSetOf<String>()

    entries().asSequence().forEach { entry ->
        if (!entries.contains(entry.name)) {
            val zae = entryFactory(entry)
            val stream = InputStreamSupplier {
                when (entry.name.substringAfterLast('.', "")) {
                    "class" -> getInputStream(entry).use { src ->
                        src.transform(transformer).inputStream()
                    }
                    else -> getInputStream(entry)
                }
            }

            creator.addArchiveEntry(zae, stream)
            entries.add(entry.name)
        } else {
            System.err.println("Duplicated jar entry: ${this.name}!/${entry.name}")
        }
    }

    ZipArchiveOutputStream(output.touch()).use { it ->
        creator.writeTo(it)
    }
}

fun ZipInputStream.transform(output: File, entryFactory: (ZipEntry) -> ZipArchiveEntry = ::ZipArchiveEntry, transformer: (ByteArray) -> ByteArray) {
    val creator = ParallelScatterZipCreator()
    val entries = mutableSetOf<String>()

    while (true) {
        val entry = nextEntry?.takeIf { true } ?: break
        if (!entries.contains(entry.name)) {
            val zae = entryFactory(entry)
            val data = readBytes()
            val stream = InputStreamSupplier {
                transformer(data).inputStream()
            }
            creator.addArchiveEntry(zae, stream)
            entries.add(entry.name)
        }
    }

    ZipArchiveOutputStream(output.touch()).use { it ->
        creator.writeTo(it)
    }
}

private const val DEFAULT_BUFFER_SIZE = 8 * 1024

private fun InputStream.readBytes(estimatedSize: Int = DEFAULT_BUFFER_SIZE): ByteArray {
    val buffer = ByteArrayOutputStream(estimatedSize.coerceAtLeast(this.available()))
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
