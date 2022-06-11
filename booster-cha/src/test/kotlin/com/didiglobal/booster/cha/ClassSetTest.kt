package com.didiglobal.booster.cha

import com.didiglobal.booster.transform.asm.asClassNode
import com.didiglobal.booster.transform.asm.className
import org.objectweb.asm.tree.ClassNode
import java.io.File
import java.io.InputStream
import kotlin.test.Test
import kotlin.test.assertTrue

object AsmClassFileParser : ClassFileParser<ClassNode> {

    override fun parse(input: InputStream): ClassNode = input.asClassNode()

    override fun getAccessFlags(classNode: ClassNode): Int = classNode.access

    override fun getInterfaces(classNode: ClassNode): Array<String> = classNode.interfaces.toTypedArray()

    override fun getSuperName(classNode: ClassNode): String? = classNode.superName

    override fun getClassName(classNode: ClassNode): String = classNode.name

}

class ClassSetTest {

    @Test
    fun `create directory backed class archive`() {
        val classes = File(javaClass.protectionDomain.codeSource.location.file)
        assertTrue(ClassSet.from(classes, AsmClassFileParser).mapNotNull { it.className }.toSet().isNotEmpty())
    }

    @Test
    fun `create jar backed class archive`() {
        val jar = File(javaClass.protectionDomain.codeSource.location.file).resolve("..${File.separator}..${File.separator}..${File.separator}libs${File.separator}${Build.ARTIFACT}-${Build.VERSION}.jar")
        assertTrue(ClassSet.from(jar.canonicalFile, AsmClassFileParser).mapNotNull { it.className }.toSet().isNotEmpty())
    }

    @Test
    fun `create clustered class archives`() {
        val classes = File(javaClass.protectionDomain.codeSource.location.file)
        val arch1 = ClassSet.from(classes, AsmClassFileParser)
        val jar = File(javaClass.protectionDomain.codeSource.location.file).resolve("..${File.separator}..${File.separator}..${File.separator}libs${File.separator}${Build.ARTIFACT}-${Build.VERSION}.jar")
        val arch2 = ClassSet.from(jar.canonicalFile, AsmClassFileParser)
        assertTrue(arch1.size > 0)
        assertTrue(arch2.size > 0)
    }

    @Test
    fun `load clustered class archives`() {
        val classes = File(javaClass.protectionDomain.codeSource.location.file)
        val arch1 = ClassSet.from(classes, AsmClassFileParser)
        val jar = File(javaClass.protectionDomain.codeSource.location.file).resolve("..${File.separator}..${File.separator}..${File.separator}libs${File.separator}${Build.ARTIFACT}-${Build.VERSION}.jar")
        val arch2 = ClassSet.from(jar.canonicalFile, AsmClassFileParser)
        (arch1 + arch2).use(ClassSet<ClassNode, AsmClassFileParser>::load)
        assertTrue(arch1.size > 0)
        assertTrue(arch2.size > 0)
    }

    @Test
    fun `load directory backed class archives`() {
        val classes = File(javaClass.protectionDomain.codeSource.location.file)
        val arch = ClassSet.from(classes, AsmClassFileParser).use(ClassSet<ClassNode, AsmClassFileParser>::load)
        assertTrue(arch.size > 0)
    }

    @Test
    fun `load jar backed class archives`() {
        val jar = File(javaClass.protectionDomain.codeSource.location.file).resolve("..${File.separator}..${File.separator}..${File.separator}libs${File.separator}${Build.ARTIFACT}-${Build.VERSION}.jar")
        val arch = ClassSet.from(jar.canonicalFile, AsmClassFileParser).use(ClassSet<ClassNode, AsmClassFileParser>::load)
        assertTrue(arch.size > 0)
    }

}