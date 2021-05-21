package com.didiglobal.booster.cha

import com.didiglobal.booster.kotlinx.green
import com.didiglobal.booster.kotlinx.search
import java.io.File
import java.io.FileNotFoundException
import java.util.stream.Collectors.toMap

private val CLASS_FILE_FILTER = { file: File -> file.extension.equals("class", true) }

/**
 * @author johnsonlee
 */
internal class DirectoryClassSet<ClassFile, ClassParser : ClassFileParser<ClassFile>>(
        val location: File,
        override val parser: ClassParser
) : AbstractClassSet<ClassFile, ClassParser>() {

    private val classes: Map<String, ClassFile> by lazy {
        location.search(CLASS_FILE_FILTER).parallelStream()
                .map(parser::parse)
                .collect(toMap(parser::getClassName) { it })
    }

    constructor(location: String, parser: ClassParser) : this(File(location).takeIf {
        it.exists()
    } ?: throw FileNotFoundException(location), parser)

    override fun get(name: String): ClassFile? = this.classes[name]

    override fun contains(name: String) = this.classes.containsKey(name)

    override val size: Int
        get() = this.classes.size

    override fun isEmpty() = this.size <= 0

    override fun load(): DirectoryClassSet<ClassFile, ClassParser> {
        println("Load ${green(this.classes.size)} classes from $location")
        return this
    }

    override fun iterator(): Iterator<ClassFile> = this.classes.values.iterator()

    override fun toString(): String = this.location.canonicalPath

    override fun close() {
    }

}
