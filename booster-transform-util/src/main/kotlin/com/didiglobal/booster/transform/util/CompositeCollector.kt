package com.didiglobal.booster.transform.util

/**
 * Represents a composite collector
 *
 * @author johnsonlee
 */
class CompositeCollector(private val collectors: Iterable<Collector<*>>) : Collector<List<*>> {

    constructor(vararg collectors: Collector<*>) : this(collectors.asIterable())

    override fun accept(name: String): Boolean {
        return collectors.any { collector ->
            collector.accept(name)
        }
    }

    override fun collect(name: String, data: () -> ByteArray): List<*> {
        val bytes by lazy(data) // to avoid end-of-stream caused by multiple readers
        return collectors.filter {
            it.accept(name)
        }.mapNotNull {
            it.collect(name) { bytes }
        }
    }

}
