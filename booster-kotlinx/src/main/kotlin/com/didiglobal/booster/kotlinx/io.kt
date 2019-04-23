package com.didiglobal.booster.kotlinx

import java.io.BufferedReader
import java.io.File
import java.io.InputStream
import java.io.InputStreamReader
import java.io.OutputStream
import java.io.Reader
import java.util.jar.JarEntry
import java.util.jar.JarFile
import java.util.jar.JarOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipFile
import java.util.zip.ZipOutputStream

fun File.file(vararg path: String) = File(this, path.joinToString(File.separator))

/**
 * Create a new file if not exists
 *
 * @author johnsonlee
 */
fun File.touch(): File {
    if (!this.exists()) {
        this.parentFile?.mkdirs()
        this.createNewFile()
    }
    return this
}

fun File.ifExists(block: (File) -> Unit) {
    if (this.exists()) {
        block(this)
    }
}

/**
 * Archive this file as a JAR
 *
 * @author johnsonlee
 */
fun File.jar(): JarFile {
    return jar(File.createTempFile("tmp-", ".jar"))
}

/**
 * Archive this file as the specified JAR
 *
 * @author johnsonlee
 */
fun File.jar(dest: File): JarFile {
    JarOutputStream(dest.outputStream()).use { output ->
        if (this.isFile) {
            output.putNextEntry(JarEntry(this.name))
            this.inputStream().use { input ->
                input.copyTo(output)
            }
        } else {
            this.parallelWalk().filter { it != this && it.isFile }.forEach { file ->
                val path = file.absolutePath.substring(this.absolutePath.length + File.separator.length)
                output.putNextEntry(JarEntry(path.separatorsToUnix()))
                file.inputStream().use { input ->
                    input.copyTo(output)
                }
            }
        }
    }
    return JarFile(dest)
}

fun File.unjar(out: File): List<File> {
    val files = mutableListOf<File>()

    JarFile(this).use { jar ->
        jar.entries().asSequence().filter { !it.isDirectory }.forEach { entry ->
            val path = entry.name.separatorsToSystem()
            jar.getInputStream(entry).use { input ->
                val file = File(out, path)
                input.redirect(file)
                files.add(file)
            }
        }
    }

    return files
}

fun File.zip(): ZipFile {
    return this.zip(File.createTempFile("tmp-", ".zip"))
}

fun File.zip(dest: File): ZipFile {
    ZipOutputStream(dest.outputStream()).use { output ->
        if (this.isFile) {
            output.putNextEntry(ZipEntry(this.name))
            this.inputStream().use { input ->
                input.copyTo(output)
            }
        } else {
            this.parallelWalk().filter { it != this && it.isFile }.forEach { file ->
                val path = file.absolutePath.substring(this.absolutePath.length + File.separator.length)
                output.putNextEntry(ZipEntry(path.separatorsToUnix()))
                file.inputStream().use { input ->
                    input.copyTo(output)
                }
            }
        }
    }
    return ZipFile(dest)
}

fun File.unzip(out: File): List<File> {
    var files = mutableListOf<File>()

    ZipFile(this).use { zip ->
        zip.entries().asSequence().filter { !it.isDirectory }.forEach { entry ->
            val path = entry.name.separatorsToSystem()
            zip.getInputStream(entry).use { input ->
                val file = File(out, path)
                input.redirect(file)
                files.add(file)
            }
        }
    }

    return files
}

/**
 * Walk file in parallel
 */
fun File.parallelWalk(): Iterable<File> {
    return FileTree(this)
}

/**
 * Return the first line of file
 */
fun File.head(): String? {
    return inputStream().use {
        it.head()
    }
}

/**
 * Returns the first line of input stream
 */
fun InputStream.head(): String? {
    return BufferedReader(InputStreamReader(this)).head()
}

/**
 * Returns the first line of reader
 */
fun Reader.head(): String? {
    return BufferedReader(this).readLine()
}

/**
 * Redirect this input stream to the specified file
 *
 * @author johnsonlee
 */
fun InputStream.redirect(file: File): Long {
    file.touch().outputStream().use {
        return this.copyTo(it)
    }
}

/**
 * Redirect this byte data to the specified file
 *
 * @author johnsonlee
 */
fun ByteArray.redirect(file: File): Long {
    this.inputStream().use {
        return it.redirect(file)
    }
}

/**
 * Redirect this byte data to the specified output stream
 *
 * @author johnsonlee
 */
fun ByteArray.redirect(output: OutputStream): Long {
    return this.inputStream().copyTo(output)
}
