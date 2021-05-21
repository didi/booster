package com.didiglobal.booster.cha

import com.didiglobal.booster.kotlinx.NCPU
import com.didiglobal.booster.kotlinx.parallelStream
import com.didiglobal.booster.kotlinx.stream
import java.util.Stack
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

/**
 * @author johnsonlee
 */
internal class CompositeClassSet<ClassFile, ClassParser : ClassFileParser<ClassFile>>(
        private val classSets: Iterable<ClassSet<ClassFile, ClassParser>>
) : AbstractClassSet<ClassFile, ClassParser>() {

    private val cache = ConcurrentHashMap<String, ClassFile>()

    override val parser: ClassParser = classSets.first().parser

    override val size: Int by lazy {
        classSets.sumBy(ClassSet<ClassFile, ClassParser>::size)
    }

    override operator fun get(name: String) = cache[name] ?: this.classSets.find {
        it.contains(name)
    }?.get(name)?.apply {
        cache[name] = this
    }

    override fun contains(name: String) = this.classSets.any { it.contains(name) }

    override fun contains(element: ClassFile) = this.classSets.any { it.contains(element) }

    override fun containsAll(elements: Collection<ClassFile>) = this.classSets.any { it.containsAll(elements) }

    override fun isEmpty() = this.size <= 0

    override fun load(): CompositeClassSet<ClassFile, ClassParser> {
        val count: (ClassSet<ClassFile, ClassParser>) -> Int = {
            var n = 1
            val stack = Stack<ClassSet<ClassFile, ClassParser>>()

            stack.push(this)

            while (stack.isNotEmpty()) {
                when (val cs = stack.pop()) {
                    is CompositeClassSet -> cs.classSets.forEach { stack.push(it) }
                    is EmptyClassSet -> Unit
                    else -> n += 1
                }
            }

            n
        }
        val executor = Executors.newFixedThreadPool(count(this).coerceAtMost(NCPU))

        try {
            classSets.map { cs ->
                executor.submit {
                    cs.load().forEach {
                        this.cache.putIfAbsent(parser.getClassName(it), it)
                    }
                }
            }.forEach {
                it.get()
            }
        } finally {
            executor.shutdown()
            executor.awaitTermination(1L, TimeUnit.HOURS)
        }

        return this
    }

    override fun iterator(): Iterator<ClassFile> = when (this.cache.size) {
        this.size -> this.cache.values.iterator()
        else -> object : Iterator<ClassFile> {
            val delegate = classSets.stream().flatMap(ClassSet<ClassFile, ClassParser>::stream).iterator()

            override fun hasNext() = delegate.hasNext()

            override fun next() = delegate.next().apply {
                cache.putIfAbsent(parser.getClassName(this), this)
            }
        }
    }

    override fun close() = this.classSets.parallelStream().forEach(ClassSet<ClassFile, ClassParser>::close)

    override fun toString() = this.classSets.joinToString(", ", "{ ", " }")

}