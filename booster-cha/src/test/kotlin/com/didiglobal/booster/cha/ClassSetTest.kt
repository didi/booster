package com.didiglobal.booster.cha

import com.didiglobal.booster.cha.Build
import com.didiglobal.booster.transform.asm.className
import java.io.File
import kotlin.test.Test
import kotlin.test.assertTrue

class ClassSetTest {

    @Test
    fun `create directory backed class archive`() {
        val classes = File(javaClass.protectionDomain.codeSource.location.file)
        assertTrue(ClassSet.from(classes).mapNotNull { it.className }.toSet().isNotEmpty())
    }

    @Test
    fun `create jar backed class archive`() {
        val jar = File(javaClass.protectionDomain.codeSource.location.file).resolve("..${File.separator}..${File.separator}..${File.separator}libs${File.separator}${Build.ARTIFACT}-${Build.VERSION}.jar")
        assertTrue(ClassSet.from(jar.canonicalFile).mapNotNull { it.className }.toSet().isNotEmpty())
    }

    @Test
    fun `create clustered class archives`() {
        val classes = File(javaClass.protectionDomain.codeSource.location.file)
        val arch1 = ClassSet.from(classes)
        val jar = File(javaClass.protectionDomain.codeSource.location.file).resolve("..${File.separator}..${File.separator}..${File.separator}libs${File.separator}${Build.ARTIFACT}-${Build.VERSION}.jar")
        val arch2 = ClassSet.from(jar.canonicalFile)

        (arch1 + arch2).parallelStream().forEach {
            println(it.className)
        }
    }

    @Test
    fun `load clustered class archives`() {
        val classes = File(javaClass.protectionDomain.codeSource.location.file)
        val arch1 = ClassSet.from(classes)
        val jar = File(javaClass.protectionDomain.codeSource.location.file).resolve("..${File.separator}..${File.separator}..${File.separator}libs${File.separator}${Build.ARTIFACT}-${Build.VERSION}.jar")
        val arch2 = ClassSet.from(jar.canonicalFile)
        (arch1 + arch2).use(ClassSet::load)
    }

    @Test
    fun `load directory backed class archives`() {
        val classes = File(javaClass.protectionDomain.codeSource.location.file)
        ClassSet.from(classes).use(ClassSet::load)
    }

    @Test
    fun `load jar backed class archives`() {
        val jar = File(javaClass.protectionDomain.codeSource.location.file).resolve("..${File.separator}..${File.separator}..${File.separator}libs${File.separator}${Build.ARTIFACT}-${Build.VERSION}.jar")
        ClassSet.from(jar.canonicalFile).use(ClassSet::load)
    }

}