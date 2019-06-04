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
