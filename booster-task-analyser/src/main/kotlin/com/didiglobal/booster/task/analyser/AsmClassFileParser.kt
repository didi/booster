package com.didiglobal.booster.task.analyser

import com.didiglobal.booster.cha.ClassFileParser
import com.didiglobal.booster.transform.asm.asClassNode
import org.objectweb.asm.tree.ClassNode
import java.io.InputStream

internal object AsmClassFileParser : ClassFileParser<ClassNode> {

    override fun parse(input: InputStream): ClassNode {
        return input.asClassNode()
    }

    override fun getClassName(classNode: ClassNode): String = classNode.name

    override fun getSuperName(classNode: ClassNode): String? = classNode.superName

    override fun getInterfaces(classNode: ClassNode): Array<String> = classNode.interfaces.toTypedArray()

    override fun getAccessFlags(classNode: ClassNode): Int = classNode.access

}