package com.didiglobal.booster.gradle

import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.Opcodes
import org.objectweb.asm.tree.ClassNode
import org.objectweb.asm.tree.LdcInsnNode
import org.objectweb.asm.util.CheckClassAdapter
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BoosterClassVisitorTest {

    private val bytecode: ByteArray = javaClass.classLoader
        .getResourceAsStream(Sample::class.java.name.replace('.', '/') + ".class")!!
        .use { it.readBytes() }

    @Test
    fun `bytecode is passed through untouched when transform is identity`() {
        val writer = ClassWriter(0)
        ClassReader(bytecode).accept(BoosterClassVisitor(Opcodes.ASM9, writer) { it }, ClassReader.EXPAND_FRAMES)
        assertEquals(bytecode.asClassNode().textify(), writer.toByteArray().asClassNode().textify())
    }

    @Test
    fun `transformed bytecode is replayed to the next visitor with expanded frames`() {
        val writer = ClassWriter(0)
        val visitor = BoosterClassVisitor(Opcodes.ASM9, CheckClassAdapter(writer, true)) { bytes ->
            val klass = bytes.asClassNode()
            klass.methods.flatMap { it.instructions.toArray().toList() }.filterIsInstance<LdcInsnNode>().forEach {
                if (it.cst == "before") it.cst = "after"
            }
            ClassWriter(ClassWriter.COMPUTE_MAXS).also(klass::accept).toByteArray()
        }
        ClassReader(bytecode).accept(visitor, ClassReader.EXPAND_FRAMES)

        val transformed = writer.toByteArray().asClassNode()
        val constants = transformed.methods.flatMap { it.instructions.toArray().toList() }.filterIsInstance<LdcInsnNode>().map { it.cst }
        assertTrue(constants.contains("after"), "constants: $constants")
        assertTrue(!constants.contains("before"), "constants: $constants")
        // frames of the branchy method survive the round trip
        val frames = transformed.methods.single { it.name == "branch" }.instructions.toArray().count { it.type == org.objectweb.asm.tree.AbstractInsnNode.FRAME }
        assertTrue(frames > 0, "no frame found")
    }

    private fun ByteArray.asClassNode() = ClassNode().also { ClassReader(this).accept(it, 0) }

    private fun ClassNode.textify(): String {
        val out = java.io.StringWriter()
        accept(org.objectweb.asm.util.TraceClassVisitor(java.io.PrintWriter(out)))
        return out.toString()
    }

}

@Suppress("unused")
class Sample {

    fun constant(): String = "before"

    fun branch(flag: Boolean): Int {
        var n = 0
        if (flag) {
            n += 1
        } else {
            n -= 1
        }
        return n
    }

}
