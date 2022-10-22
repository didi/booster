package com.didiglobal.booster.cha

import com.didiglobal.booster.kotlinx.NCPU
import com.didiglobal.booster.kotlinx.green
import com.didiglobal.booster.kotlinx.search
import com.didiglobal.booster.kotlinx.yellow
import io.johnsonlee.once.Once
import java.io.File
import java.io.FileNotFoundException
import java.net.URL
import java.time.Duration
import java.util.concurrent.Callable
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

private val CLASS_FILE_FILTER = { file: File -> file.extension.equals("class", true) }

/**
 * @author johnsonlee
 */
internal class DirectoryClassSet<ClassFile, ClassParser>(
        private val location: File,
        private val parser: ClassParser
) : AbstractClassSet<ClassFile, ClassParser>(), ClassFileParser<ClassFile> by parser
        where ClassParser : ClassFileParser<ClassFile> {

    private val once = Once<DirectoryClassSet<ClassFile, ClassParser>>()

    private val classes: Map<String, ClassFile> by lazy {
        val files = location.search(CLASS_FILE_FILTER).takeIf {
            it.isNotEmpty()
        } ?: return@lazy emptyMap<String, ClassFile>()
        val executor = Executors.newFixedThreadPool(files.size.coerceAtMost(NCPU))

        try {
            files.map { file ->
                executor.submit(Callable {
                    parser.parse(file)?.let { clazz ->
                        parser.getClassName(clazz) to clazz
                    }
                })
            }.mapNotNull {
                it.get()
            }.toMap()
        } finally {
            executor.shutdown()
            executor.awaitTermination(1, TimeUnit.HOURS)
        }
    }

    private val snapshot: Set<ClassFile> by lazy {
        classes.values.toSet()
    }

    constructor(location: String, parser: ClassParser) : this(File(location).takeIf {
        it.exists()
    } ?: throw FileNotFoundException(location), parser)

    override val size: Int by lazy {
        load().classes.size
    }

    override val classpath: List<URL> by lazy {
        listOf(location.toURI().toURL())
    }

    override fun get(name: String): ClassFile? = load().classes[name]

    override fun contains(name: String) = load().classes.containsKey(name)

    override fun contains(element: ClassFile): Boolean = load().snapshot.contains(element)

    override fun isEmpty() = this.size <= 0

    override fun load(): DirectoryClassSet<ClassFile, ClassParser> = once {
        val t0 = System.nanoTime()
        val size = classes.size
        val t1 = System.nanoTime()
        println("Load ${green(size)} classes from $location in ${yellow(Duration.ofNanos(t1 - t0).toMillis())} ms")
        this
    }

    override fun iterator(): Iterator<ClassFile> = load().classes.values.iterator()

    override fun toString(): String = location.canonicalPath

    override fun close() = Unit

}
