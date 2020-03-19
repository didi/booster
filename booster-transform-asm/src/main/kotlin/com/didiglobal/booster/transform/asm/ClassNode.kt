package com.didiglobal.booster.transform.asm

import org.objectweb.asm.Opcodes
import org.objectweb.asm.tree.ClassNode
import org.objectweb.asm.tree.InsnList
import org.objectweb.asm.tree.InsnNode
import org.objectweb.asm.tree.MethodInsnNode
import org.objectweb.asm.tree.MethodNode
import org.objectweb.asm.tree.VarInsnNode

/**
 * The simple name of class
 */
val ClassNode.simpleName: String
    get() = this.name.substring(this.name.lastIndexOf('/') + 1)

/**
 * The name of class
 */
val ClassNode.className: String
    get() = name.replace('/', '.')

val ClassNode.isAnnotation: Boolean
    get() = 0 != (access and Opcodes.ACC_ANNOTATION)

val ClassNode.isInterface: Boolean
    get() = 0 != (access and Opcodes.ACC_INTERFACE)

val ClassNode.isAbstract: Boolean
    get() = 0 != (access and Opcodes.ACC_ABSTRACT)

val ClassNode.isPublic: Boolean
    get() = 0 != (access and Opcodes.ACC_PUBLIC)

val ClassNode.isProtected: Boolean
    get() = 0 != (access and Opcodes.ACC_PROTECTED)

val ClassNode.isPrivate: Boolean
    get() = 0 != (access and Opcodes.ACC_PRIVATE)

val ClassNode.isStatic: Boolean
    get() = 0 != (access and Opcodes.ACC_STATIC)

val ClassNode.isFinal: Boolean
    get() = 0 != (access and Opcodes.ACC_FINAL)

fun ClassNode.isInvisibleAnnotationPresent(annotations: Iterable<String>) = this.invisibleAnnotations?.map {
    it.desc
}?.any(annotations::contains) ?: false

fun ClassNode.isInvisibleAnnotationPresent(vararg annotations: String) = isInvisibleAnnotationPresent(annotations.asIterable())


fun ClassNode.isVisibleAnnotationPresent(annotations: Iterable<String>) = this.visibleAnnotations?.map {
    it.desc
}?.any(annotations::contains) ?: false

fun ClassNode.isVisibleAnnotationPresent(vararg annotations: String) = isVisibleAnnotationPresent(annotations.asIterable())

val ClassNode.defaultClinit: MethodNode
    get() = MethodNode(Opcodes.ACC_STATIC, "<clinit>", "()V", null, null).apply {
        maxStack = 1
        instructions.add(InsnNode(Opcodes.RETURN))
    }

val ClassNode.defaultInit: MethodNode
    get() = MethodNode(Opcodes.ACC_PUBLIC, "<init>", "()V", null, null).apply {
        maxStack = 1
        instructions.add(InsnList().apply {
            add(VarInsnNode(Opcodes.ALOAD, 0))
            add(MethodInsnNode(Opcodes.INVOKESPECIAL, superName, name, desc, false))
            add(InsnNode(Opcodes.RETURN))
        })
    }

val ClassNode.defaultOnCreate: MethodNode
    get() = MethodNode(Opcodes.ACC_PUBLIC, "onCreate", "()V", null, null).apply {
        instructions.add(InsnList().apply {
            add(VarInsnNode(Opcodes.ALOAD, 0))
            add(MethodInsnNode(Opcodes.INVOKESPECIAL, superName, name, desc, false))
            add(InsnNode(Opcodes.RETURN))
        })
        maxStack = 1
    }
