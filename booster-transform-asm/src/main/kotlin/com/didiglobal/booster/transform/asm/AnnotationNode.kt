package com.didiglobal.booster.transform.asm

import org.objectweb.asm.tree.AnnotationNode

@Suppress("UNCHECKED_CAST")
fun <T> AnnotationNode.getValue(name: String = "value"): T? = values?.withIndex()?.iterator()?.let {
    while (it.hasNext()) {
        val i = it.next()
        if (i.index % 2 == 0 && i.value == name) {
            return@let it.next().value as T
        }
    }
    null
}
