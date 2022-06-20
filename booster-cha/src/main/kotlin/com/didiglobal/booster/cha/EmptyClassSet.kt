package com.didiglobal.booster.cha

import java.net.URL

/**
 * @author johnsonlee
 */
@Suppress("UNCHECKED_CAST")
internal class EmptyClassSet<ClassFile, ClassParser>
    : AbstractClassSet<ClassFile, ClassParser>()
    , ClassFileParser<ClassFile> by NullClassFileParser as ClassFileParser<ClassFile>
        where ClassParser : ClassFileParser<ClassFile> {

    override val size: Int = 0

    override val classpath: List<URL> = emptyList()

    override fun isEmpty() = true

    override fun get(name: String): ClassFile? = null

    override fun contains(name: String) = false

    override fun contains(element: ClassFile) = false

    override fun containsAll(elements: Collection<ClassFile>) = false

    override fun iterator() = object : Iterator<ClassFile> {

        override fun hasNext() = false

        override fun next() = throw NoSuchElementException()

    }

    override fun close() {}

}
