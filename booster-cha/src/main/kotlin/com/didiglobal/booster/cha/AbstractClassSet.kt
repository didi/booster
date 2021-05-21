package com.didiglobal.booster.cha

/**
 * @author johnsonlee
 */
abstract class AbstractClassSet<ClassFile, ClassParser : ClassFileParser<ClassFile>> : ClassSet<ClassFile, ClassParser> {

    override fun contains(element: ClassFile) = contains(parser.getClassName(element))

    override fun containsAll(elements: Collection<ClassFile>) = elements.all {
        contains(parser.getClassName(it))
    }

    override fun load() = this

}