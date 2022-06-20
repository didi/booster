package com.didiglobal.booster.cha

import com.didiglobal.booster.kotlinx.green
import com.didiglobal.booster.kotlinx.yellow
import io.johnsonlee.once.Once
import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.net.URL
import java.time.Duration
import java.util.zip.ZipInputStream

/**
 * @author johnsonlee
 */
internal class ArchivedClassSet<ClassFile, ClassParser : ClassFileParser<ClassFile>>(
        private val location: File,
        parser: ClassParser
) : AbstractClassSet<ClassFile, ClassParser>(), ClassFileParser<ClassFile> by parser {

    private val once = Once<ArchivedClassSet<ClassFile, ClassParser>>()

    private val classes: Map<String, ClassFile> by lazy {
        ZipInputStream(FileInputStream(location).buffered()).use { zip ->
            loadClasses(zip).associateByTo(mutableMapOf(), parser::getClassName)
        }
    }

    private val snapshot: Set<ClassFile> by lazy {
        classes.values.toSet()
    }

    constructor(location: String, parser: ClassParser) : this(File(location).takeIf {
        it.exists()
    } ?: throw FileNotFoundException(location), parser)

    private fun loadClasses(zip: ZipInputStream): List<ClassFile> {
        val classes = mutableListOf<ClassFile>()
        while (true) {
            val entry = zip.nextEntry ?: break
            classes += when {
                entry.name.endsWith(".class", true) -> listOf(parse(zip))
                entry.name == "classes.jar" -> loadClasses(ZipInputStream(zip))
                else -> emptyList()
            }
        }
        return classes
    }

    override val size: Int by lazy {
        load().classes.size
    }

    override val classpath: List<URL> by lazy {
        listOf(location.toURI().toURL())
    }

    override fun get(name: String) = load().classes[name]

    override fun contains(name: String) = load().classes.containsKey(name)

    override fun contains(element: ClassFile): Boolean = load().snapshot.contains(element)

    override fun isEmpty() = size <= 0

    override fun load(): ArchivedClassSet<ClassFile, ClassParser> = once {
        val t0 = System.nanoTime()
        val size = classes.size
        val t1 = System.nanoTime()
        println("Load ${green(size)} classes from $location in ${yellow(Duration.ofNanos(t1 - t0).toMillis())} ms")
        this
    }

    override fun iterator(): Iterator<ClassFile> = load().classes.values.iterator()

    override fun close() = Unit

    override fun toString(): String = location.canonicalPath

}