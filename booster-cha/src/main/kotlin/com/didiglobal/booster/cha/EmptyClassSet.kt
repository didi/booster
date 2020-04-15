package com.didiglobal.booster.cha

import org.objectweb.asm.tree.ClassNode

/**
 * @author johnsonlee
 */
internal class EmptyClassSet : AbstractClassSet() {

    override val size: Int = 0

    override fun isEmpty() = true

    override fun get(name: String): ClassNode? = null

    override fun contains(name: String) = false

    override fun contains(element: ClassNode) = false

    override fun containsAll(elements: Collection<ClassNode>) = false

    override fun iterator() = object : Iterator<ClassNode> {

        override fun hasNext() = false

        override fun next() = throw NoSuchElementException()

    }

    override fun close() {}

}
