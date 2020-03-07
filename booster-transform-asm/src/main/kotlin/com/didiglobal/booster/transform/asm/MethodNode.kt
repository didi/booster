package com.didiglobal.booster.transform.asm

import org.objectweb.asm.tree.MethodNode

fun MethodNode.isInvisibleAnnotationPresent(vararg annotations: String) = this.invisibleAnnotations?.map {
    it.desc
}?.any(annotations::contains) ?: false

fun MethodNode.isVisibleAnnotationPresent(vararg annotations: String) = this.visibleAnnotations?.map {
    it.desc
}?.any(annotations::contains) ?: false
