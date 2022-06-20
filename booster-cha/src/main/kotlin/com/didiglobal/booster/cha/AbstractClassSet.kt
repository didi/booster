package com.didiglobal.booster.cha

/**
 * @author johnsonlee
 */
abstract class AbstractClassSet<ClassFile, ClassParser> : ClassSet<ClassFile, ClassParser>
        where ClassParser : ClassFileParser<ClassFile> {

    override fun containsAll(elements: Collection<ClassFile>) = elements.all(::contains)

    override fun load() = this

}