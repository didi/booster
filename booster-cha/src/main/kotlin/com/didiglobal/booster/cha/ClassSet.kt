package com.didiglobal.booster.cha

import com.didiglobal.booster.kotlinx.isEmpty
import com.didiglobal.booster.kotlinx.red
import java.io.Closeable
import java.io.File
import java.net.URL

/**
 * @author johnsonlee
 */
interface ClassSet<ClassFile, ClassParser> : Set<ClassFile>, ClassFileParser<ClassFile>, Closeable
        where ClassParser : ClassFileParser<ClassFile> {

    operator fun get(name: String): ClassFile?

    operator fun plus(classSet: ClassSet<ClassFile, ClassParser>): ClassSet<ClassFile, ClassParser> = of(this, classSet)

    operator fun contains(name: String): Boolean

    val classpath: List<URL>

    fun load(): ClassSet<ClassFile, ClassParser>

    companion object {

        private val ARCHIVES = Regex("^(aar)|(zip)|(jar)$", RegexOption.IGNORE_CASE)

        fun <ClassFile, ClassParser : ClassFileParser<ClassFile>> from(
                file: File,
                parser: ClassParser
        ): ClassSet<ClassFile, ClassParser> = when {
            file.isDirectory -> DirectoryClassSet(file, parser)
            file.extension matches ARCHIVES -> ArchivedClassSet(file, parser)
            else -> {
                System.err.println(red("unsupported file: $file"))
                EmptyClassSet()
            }
        }

        fun <ClassFile, ClassParser : ClassFileParser<ClassFile>> of(
                vararg classSets: ClassSet<ClassFile, ClassParser>
        ): ClassSet<ClassFile, ClassParser> = of(classSets.asIterable())

        fun <ClassFile, ClassParser : ClassFileParser<ClassFile>> of(
                classSets: Iterable<ClassSet<ClassFile, ClassParser>>
        ): ClassSet<ClassFile, ClassParser> = when {
            classSets.isEmpty() -> EmptyClassSet()
            classSets.count() == 1 -> classSets.first()
            else -> CompositeClassSet(classSets)
        }

    }

}

fun <ClassFile, ClassParser : ClassFileParser<ClassFile>> Iterable<ClassSet<ClassFile, ClassParser>>.fold() = ClassSet.of(this)