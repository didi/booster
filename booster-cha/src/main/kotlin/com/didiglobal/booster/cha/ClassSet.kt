package com.didiglobal.booster.cha

import com.didiglobal.booster.kotlinx.isEmpty
import com.didiglobal.booster.kotlinx.red
import org.objectweb.asm.tree.ClassNode
import java.io.Closeable
import java.io.File

/**
 * @author johnsonlee
 */
interface ClassSet : Set<ClassNode>, Closeable {

    operator fun get(name: String): ClassNode?

    operator fun plus(classSet: ClassSet): ClassSet = of(this, classSet)

    fun contains(name: String): Boolean

    fun load(): ClassSet

    companion object {

        private val ARCHIVES = Regex("^(zip)|(jar)$", RegexOption.IGNORE_CASE)

        fun from(file: File): ClassSet = when {
            file.isDirectory -> DirectoryClassSet(file)
            file.extension matches ARCHIVES -> ArchivedClassSet(file)
            else -> {
                System.err.println(red("unsupported file: $file"))
                EmptyClassSet()
            }
        }

        fun of(vararg classSets: ClassSet): ClassSet = of(classSets.asIterable())

        fun of(classSets: Iterable<ClassSet>): ClassSet = when {
            classSets.isEmpty() -> EmptyClassSet()
            classSets.count() == 1 -> classSets.first()
            else -> CompositeClassSet(classSets)
        }

    }

}

fun Iterable<ClassSet>.fold() = ClassSet.of(this)