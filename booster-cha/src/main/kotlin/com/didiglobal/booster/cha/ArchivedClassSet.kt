package com.didiglobal.booster.cha

import com.didiglobal.booster.kotlinx.green
import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.util.zip.ZipInputStream

/**
 * @author johnsonlee
 */
internal class ArchivedClassSet<ClassFile, ClassParser : ClassFileParser<ClassFile>>(
        val location: File,
        override val parser: ClassParser
) : AbstractClassSet<ClassFile, ClassParser>() {

    private val classes: Map<String, ClassFile> by lazy {
        ZipInputStream(FileInputStream(location)).use { zip ->
            loadClasses(zip).associateByTo(mutableMapOf(), parser::getClassName)
        }
    }

    constructor(location: String, parser: ClassParser) : this(File(location).takeIf {
        it.exists()
    } ?: throw FileNotFoundException(location), parser)

    private fun loadClasses(zip: ZipInputStream): List<ClassFile> {
        val classes = mutableListOf<ClassFile>()
        while (true) {
            val entry = zip.nextEntry ?: break
            classes += when {
                entry.name.endsWith(".class", true) -> listOf(parser.parse(zip))
                entry.name == "classes.jar" -> loadClasses(ZipInputStream(zip))
                else -> emptyList()
            }
        }
        return classes
    }

    override fun get(name: String) = this.classes[name]

    override fun contains(name: String) = this.classes.containsKey(name)

    override val size: Int
        get() = this.classes.size

    override fun isEmpty() = this.size <= 0

    override fun load(): ArchivedClassSet<ClassFile, ClassParser> {
        println("Load ${green(this.classes.size)} classes from $location")
        return this
    }

    override fun iterator(): Iterator<ClassFile> = this.classes.values.iterator()

    override fun close() = Unit

    override fun toString(): String = this.location.canonicalPath

}