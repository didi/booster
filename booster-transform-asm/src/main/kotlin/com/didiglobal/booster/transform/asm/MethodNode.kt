package com.didiglobal.booster.transform.asm

import org.objectweb.asm.Opcodes
import org.objectweb.asm.tree.MethodNode

val MethodNode.args: String
    get() = desc.substring(desc.indexOf('(') + 1, desc.lastIndexOf(')'))

val MethodNode.isAbstract: Boolean
    get() = 0 != (access and Opcodes.ACC_ABSTRACT)

val MethodNode.isPublic: Boolean
    get() = 0 != (access and Opcodes.ACC_PUBLIC)

val MethodNode.isProtected: Boolean
    get() = 0 != (access and Opcodes.ACC_PROTECTED)

val MethodNode.isPrivate: Boolean
    get() = 0 != (access and Opcodes.ACC_PRIVATE)

val MethodNode.isNative: Boolean
    get() = 0 != (access and Opcodes.ACC_NATIVE)

val MethodNode.isStatic: Boolean
    get() = 0 != (access and Opcodes.ACC_STATIC)

fun MethodNode.isInvisibleAnnotationPresent(vararg annotations: String) = isInvisibleAnnotationPresent(annotations.asIterable())

fun MethodNode.isInvisibleAnnotationPresent(annotations: Iterable<String>) = this.invisibleAnnotations?.map {
    it.desc
}?.any(annotations::contains) ?: false

fun MethodNode.isVisibleAnnotationPresent(vararg annotations: String) = isVisibleAnnotationPresent(annotations.asIterable())

fun MethodNode.isVisibleAnnotationPresent(annotations: Iterable<String>) = this.visibleAnnotations?.map {
    it.desc
}?.any(annotations::contains) ?: false
