package com.didiglobal.booster.cha

import java.io.File
import java.io.InputStream
import java.lang.reflect.Modifier

interface ClassFileParser<ClassNode> {

    fun parse(file: File): ClassNode = file.inputStream().buffered().use(this::parse)

    fun parse(input: InputStream): ClassNode

    fun getClassName(classNode: ClassNode): String

    fun getSuperName(classNode: ClassNode): String?

    fun getInterfaces(classNode: ClassNode): Array<String>

    fun getAccessFlags(classNode: ClassNode): Int

    fun isInterface(classNode: ClassNode): Boolean = Modifier.isInterface(getAccessFlags(classNode))

    fun isFinal(classNode: ClassNode): Boolean = Modifier.isFinal(getAccessFlags(classNode))

}