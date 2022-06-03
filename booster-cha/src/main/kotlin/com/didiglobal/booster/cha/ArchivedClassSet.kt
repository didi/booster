package com.didiglobal.booster.cha

import com.didiglobal.booster.kotlinx.asIterable
import com.didiglobal.booster.kotlinx.green
import com.didiglobal.booster.kotlinx.parallelStream
import java.io.File
import java.io.FileNotFoundException
import java.util.stream.Collectors.toMap
import java.util.zip.ZipEntry
import java.util.zip.ZipFile

private val CLASS_ENTRY_FILTER = { entry: ZipEntry -> entry.name.endsWith(".class", true) }

/**
 * @author johnsonlee
 */
internal class ArchivedClassSet<ClassFile, ClassParser : ClassFileParser<ClassFile>>(
        val location: File,
        override val parser: ClassParser
) : AbstractClassSet<ClassFile, ClassParser>() {

    private val classes: Map<String, ClassFile> by lazy {
        ZipFile(location).use { zip ->
            zip.entries().iterator().asIterable().parallelStream().filter(CLASS_ENTRY_FILTER).map { entry ->
                parser.parse(zip.getInputStream(entry))
            }.collect(toMap(parser::getClassName) { it })
        }
    }

    constructor(location: String, parser: ClassParser) : this(File(location).takeIf {
        it.exists()
    } ?: throw FileNotFoundException(location), parser)

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