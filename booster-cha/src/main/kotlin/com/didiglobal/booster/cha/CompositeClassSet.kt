package com.didiglobal.booster.cha

import com.didiglobal.booster.kotlinx.NCPU
import com.didiglobal.booster.kotlinx.green
import com.didiglobal.booster.kotlinx.parallelStream
import com.didiglobal.booster.kotlinx.yellow
import io.johnsonlee.once.Once
import java.io.IOException
import java.io.InputStream
import java.net.URL
import java.time.Duration
import java.util.Stack
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

/**
 * @author johnsonlee
 */
internal class CompositeClassSet<ClassFile, ClassParser>(
        private val classSets: Iterable<ClassSet<ClassFile, ClassParser>>
) : AbstractClassSet<ClassFile, ClassParser>() where ClassParser : ClassFileParser<ClassFile> {

    private val nameToClass = ConcurrentHashMap<String, ClassFile>()

    private val once = Once<CompositeClassSet<ClassFile, ClassParser>>()

    override val size: Int by lazy {
        load().classSets.sumOf(ClassSet<ClassFile, ClassParser>::size)
    }

    override val classpath: List<URL> by lazy {
        classSets.map(ClassSet<ClassFile, ClassParser>::classpath).flatten()
    }

    override operator fun get(name: String): ClassFile? = when (size) {
        0 -> null
        1 -> find(name)
        else -> nameToClass[name] ?: find(name)?.also {
            nameToClass[name] = it
        }
    }

    override fun parse(input: InputStream): ClassFile = delegate {
        parse(input)
    } ?: throw IOException("Parse class from stream failed: $input")

    override fun getClassName(classNode: ClassFile): String = delegate(classNode) {
        getClassName(classNode)
    }

    override fun getSuperName(classNode: ClassFile): String? = delegate(classNode) {
        getSuperName(classNode)
    }

    override fun getInterfaces(classNode: ClassFile): Array<String> = delegate(classNode) {
        getInterfaces(classNode)
    }

    override fun getAccessFlags(classNode: ClassFile): Int = delegate(classNode) {
        getAccessFlags(classNode)
    }

    override fun contains(name: String) = load().classSets.any {
        it.contains(name)
    }

    override fun contains(element: ClassFile) = load().classSets.any {
        it.contains(element)
    }

    override fun containsAll(elements: Collection<ClassFile>) = load().classSets.any {
        it.containsAll(elements)
    }

    override fun isEmpty() = this.size <= 0

    override fun load(): CompositeClassSet<ClassFile, ClassParser> = once {
        val t0 = System.nanoTime()
        val count: (ClassSet<ClassFile, ClassParser>) -> Int = { root ->
            val stack = Stack<ClassSet<ClassFile, ClassParser>>().apply {
                push(root)
            }
            var n = 0

            while (stack.isNotEmpty()) {
                when (val cs = stack.pop()) {
                    is CompositeClassSet -> cs.classSets.forEach { stack.push(it) }
                    is EmptyClassSet -> Unit
                    else -> n += 1
                }
            }

            n
        }

        val n = count(this)
        if (n >= 1) {
            val executor = Executors.newFixedThreadPool(n.coerceAtMost(NCPU))

            try {
                classSets.map { cs ->
                    executor.submit {
                        cs.load().forEach { clazz ->
                            nameToClass.putIfAbsent(cs.getClassName(clazz), clazz)
                        }
                    }
                }.forEach {
                    it.get()
                }
            } finally {
                executor.shutdown()
                executor.awaitTermination(1L, TimeUnit.HOURS)
            }
        }
        val t1 = System.nanoTime()
        val size = classSets.sumOf(ClassSet<ClassFile, ClassParser>::size)
        println(classpath.joinToString("\n", "Load ${green(size)} classes from ${green(n)} class sets in ${yellow(Duration.ofNanos(t1 - t0).toMillis())} ms\n") {
            "  ✨ $it"
        })

        this
    }

    override fun iterator(): Iterator<ClassFile> = load().classSets.asSequence().flatMap {
        it.asSequence()
    }.iterator()

    override fun close() = this.classSets.parallelStream().forEach(ClassSet<ClassFile, ClassParser>::close)

    override fun toString() = this.classSets.joinToString(", ", "{ ", " }")

    private fun find(name: String): ClassFile? {
        val it = load().classSets.iterator()
        while (it.hasNext()) {
            val clazz = it.next()[name]
            if (clazz != null) return clazz
        }
        return null
    }

    private fun <R> delegate(block: ClassSet<ClassFile, ClassParser>.() -> R): R? {
        val it = classSets.iterator()
        while (it.hasNext()) {
            val next = it.next()
            try {
                return next.block()
            } catch (e: Throwable) {
                continue
            }
        }
        return null
    }

    private fun <R> delegate(classNode: ClassFile, block: ClassSet<ClassFile, ClassParser>.() -> R): R {
        val classSet = load().classSets.find {
            it.contains(classNode)
        } ?: throw IllegalArgumentException("Unknown class node $classNode")
        return classSet.run(block)
    }

}