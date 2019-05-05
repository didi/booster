package com.didiglobal.booster.transform.asm

import org.objectweb.asm.tree.AbstractInsnNode

fun AbstractInsnNode.find(predicate: (AbstractInsnNode) -> Boolean): AbstractInsnNode? {
    var next: AbstractInsnNode? = this

    while (null != next) {
        if (predicate(next)) {
            return next
        }
        next = next.next
    }

    return null
}

inline fun <reified T> AbstractInsnNode.isInstanceOf(action: (T) -> Unit) {
    if (this is T) {
        action(this as T)
    }
}
