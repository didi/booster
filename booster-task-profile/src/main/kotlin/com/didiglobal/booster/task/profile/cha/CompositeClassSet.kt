package com.didiglobal.booster.task.profile.cha

import com.didiglobal.booster.kotlinx.NCPU
import com.didiglobal.booster.kotlinx.parallelStream
import com.didiglobal.booster.kotlinx.stream
import org.objectweb.asm.tree.ClassNode
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

/**
 * @author johnsonlee
 */
internal class CompositeClassSet(private val classSets: Iterable<ClassSet>) : AbstractClassSet() {

    private val cache = ConcurrentHashMap<String, ClassNode>()

    override val size: Int by lazy {
        classSets.sumBy(ClassSet::size)
    }

    override operator fun get(name: String) = cache[name] ?: this.classSets.find {
        it.contains(name)
    }?.get(name)?.apply {
        cache[name] = this
    }

    override fun contains(name: String) = this.classSets.any { it.contains(name) }

    override fun contains(element: ClassNode) = this.classSets.any { it.contains(element) }

    override fun containsAll(elements: Collection<ClassNode>) = this.classSets.any { it.containsAll(elements) }

    override fun isEmpty() = this.size <= 0

    override fun load(): CompositeClassSet {
        val executor = Executors.newWorkStealingPool(NCPU)

        try {
            classSets.map { cs ->
                executor.submit {
                    cs.load()
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

    override fun iterator(): Iterator<ClassNode> = object : Iterator<ClassNode> {
        val delegate = classSets.stream().flatMap(ClassSet::stream).iterator()

        override fun hasNext() = delegate.hasNext()

        override fun next() = delegate.next().apply {
            cache[name] = this
        }
    }

    override fun close() = this.classSets.parallelStream().forEach(ClassSet::close)

    override fun toString() = this.classSets.joinToString(", ", "{ ", " }")

}