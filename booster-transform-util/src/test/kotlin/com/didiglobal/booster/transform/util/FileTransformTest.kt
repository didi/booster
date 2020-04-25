package com.didiglobal.booster.transform.util

import com.didiglobal.booster.kotlinx.file
import java.io.File
import java.nio.file.Files
import java.util.jar.JarFile
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

private val PWD: String = System.getProperty("user.dir")
private val BUILD_DIR = File(PWD, "build")

class FileTransformTest {

    @Test
    fun `transform jar`() {
        val jar = "${Build.ARTIFACT}-${Build.VERSION}.jar"
        val src = BUILD_DIR.file("libs", jar)
        val dest = Files.createTempDirectory(Build.GROUP).toFile().file(jar)

        src.transform(dest) { it }

        JarFile(src).use { a ->
            JarFile(dest).use { b ->
                val aEntries = a.entries().asSequence().sortedBy { it.name }.toList()
                val bEntries = b.entries().asSequence().sortedBy { it.name }.toList()
                assertEquals(aEntries.size, bEntries.size)
                assertEquals(aEntries.map { it.name }, bEntries.map { it.name })
                assertEquals(aEntries.map { it.size }, bEntries.map { it.size })
            }
        }
    }

    @Test
    fun `transform jar with duplicated entries`() {
        val jar = javaClass.classLoader.getResource("pinyin4j-2.5.0.jar")
        val src = File(jar!!.file)
        val dest = Files.createTempDirectory(Build.GROUP).toFile().file(src.name)
        src.transform(dest)
        assertTrue { dest.exists() }
        println(dest)
    }

}
