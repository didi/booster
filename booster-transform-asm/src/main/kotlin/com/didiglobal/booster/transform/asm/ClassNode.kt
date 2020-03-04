package com.didiglobal.booster.transform.asm

import org.objectweb.asm.tree.ClassNode

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

fun ClassNode.isInvisibleAnnotationPresent(vararg annotations: String) = this.invisibleAnnotations?.map {
    it.desc
}?.any(annotations::contains) ?: false

fun ClassNode.isVisibleAnnotationPresent(vararg annotations: String) = this.visibleAnnotations?.map {
    it.desc
}?.any(annotations::contains) ?: false
