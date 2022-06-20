package com.didiglobal.booster.cha.asm

import com.didiglobal.booster.cha.ClassFileParser
import org.objectweb.asm.ClassReader
import org.objectweb.asm.tree.ClassNode
import java.io.InputStream

object AsmClassFileParser : ClassFileParser<ClassNode> {

    override fun parse(input: InputStream): ClassNode = ClassNode().also { klass ->
        ClassReader(input.readBytes()).accept(klass, 0)
    }

    override fun getAccessFlags(classNode: ClassNode): Int = classNode.access

    override fun getInterfaces(classNode: ClassNode): Array<String> = classNode.interfaces.toTypedArray()

    override fun getSuperName(classNode: ClassNode): String? = classNode.superName

    override fun getClassName(classNode: ClassNode): String = classNode.name

}