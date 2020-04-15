package com.didiglobal.booster.cha

import com.didiglobal.booster.transform.asm.className
import org.objectweb.asm.tree.ClassNode

/**
 * @author johnsonlee
 */
abstract class AbstractClassSet : ClassSet {

    override fun contains(element: ClassNode) = contains(element.className)

    override fun containsAll(elements: Collection<ClassNode>) = elements.all {
        contains(it.className)
    }

    override fun load() = this

}