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

    fun isAbstract(classNode: ClassNode): Boolean = Modifier.isAbstract(getAccessFlags(classNode))

    fun isPublic(classNode: ClassNode): Boolean = Modifier.isPublic(getAccessFlags(classNode))

    fun isProtected(classNode: ClassNode): Boolean = Modifier.isProtected(getAccessFlags(classNode))

    fun isPrivate(classNode: ClassNode): Boolean = Modifier.isPrivate(getAccessFlags(classNode))

    fun isStatic(classNode: ClassNode): Boolean = Modifier.isStatic(getAccessFlags(classNode))

    fun isStrict(classNode: ClassNode): Boolean = Modifier.isStrict(getAccessFlags(classNode))

}