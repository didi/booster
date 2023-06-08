package com.didiglobal.booster.kotlinx

import java.util.stream.Stream
import java.util.stream.StreamSupport

fun <T> Iterable<T>.isEmpty() = !iterator().hasNext()

inline fun <K, V> Map<K, V>.ifNotEmpty(action: (Map<K, V>) -> Unit): Map<K, V> {
    if (isNotEmpty()) {
        action(this)
    }
    return this
}

inline fun <T> Collection<T>.ifNotEmpty(action: (Collection<T>) -> Unit): Collection<T> {
    if (isNotEmpty()) {
        action(this)
    }
    return this
}

fun <T> Iterator<T>.asIterable(): Iterable<T> = Iterable { this }

fun <T> Iterator<T>.stream(): Stream<T> = asIterable().stream()

fun <T> Iterator<T>.parallelStream(): Stream<T> = asIterable().parallelStream()

fun <T> Iterable<T>.stream(): Stream<T> = StreamSupport.stream(spliterator(), false)

fun <T> Iterable<T>.parallelStream(): Stream<T> = StreamSupport.stream(spliterator(), true)

/*** Kotlin Version Compat Start ***/
/**
 * Returns the largest value among all values produced by [selector] function
 * applied to each element in the collection.
 *
 * If any of values produced by [selector] function is `NaN`, the returned result is `NaN`.
 *
 * @throws NoSuchElementException if the collection is empty.
 */
public inline fun <T> Iterable<T>.maxOf(selector: (T) -> Double): Double {
    val iterator = iterator()
    if (!iterator.hasNext()) throw NoSuchElementException()
    var maxValue = selector(iterator.next())
    while (iterator.hasNext()) {
        val v = selector(iterator.next())
        maxValue = maxOf(maxValue, v)
    }
    return maxValue
}

/**
 * Returns the largest value among all values produced by [selector] function
 * applied to each element in the collection.
 *
 * If any of values produced by [selector] function is `NaN`, the returned result is `NaN`.
 *
 * @throws NoSuchElementException if the collection is empty.
 */
public inline fun <T> Iterable<T>.maxOf(selector: (T) -> Float): Float {
    val iterator = iterator()
    if (!iterator.hasNext()) throw NoSuchElementException()
    var maxValue = selector(iterator.next())
    while (iterator.hasNext()) {
        val v = selector(iterator.next())
        maxValue = maxOf(maxValue, v)
    }
    return maxValue
}

/**
 * Returns the largest value among all values produced by [selector] function
 * applied to each element in the collection.
 *
 * @throws NoSuchElementException if the collection is empty.
 */
public inline fun <T, R : Comparable<R>> Iterable<T>.maxOf(selector: (T) -> R): R {
    val iterator = iterator()
    if (!iterator.hasNext()) throw NoSuchElementException()
    var maxValue = selector(iterator.next())
    while (iterator.hasNext()) {
        val v = selector(iterator.next())
        if (maxValue < v) {
            maxValue = v
        }
    }
    return maxValue
}

/**
 * Returns the largest value among all values produced by [selector] function
 * applied to each element in the collection or `null` if there are no elements.
 *
 * If any of values produced by [selector] function is `NaN`, the returned result is `NaN`.
 */
public inline fun <T> Iterable<T>.maxOfOrNull(selector: (T) -> Double): Double? {
    val iterator = iterator()
    if (!iterator.hasNext()) return null
    var maxValue = selector(iterator.next())
    while (iterator.hasNext()) {
        val v = selector(iterator.next())
        maxValue = maxOf(maxValue, v)
    }
    return maxValue
}

/**
 * Returns the largest value among all values produced by [selector] function
 * applied to each element in the collection or `null` if there are no elements.
 *
 * If any of values produced by [selector] function is `NaN`, the returned result is `NaN`.
 */
public inline fun <T> Iterable<T>.maxOfOrNull(selector: (T) -> Float): Float? {
    val iterator = iterator()
    if (!iterator.hasNext()) return null
    var maxValue = selector(iterator.next())
    while (iterator.hasNext()) {
        val v = selector(iterator.next())
        maxValue = maxOf(maxValue, v)
    }
    return maxValue
}

/**
 * Returns the largest value among all values produced by [selector] function
 * applied to each element in the collection or `null` if there are no elements.
 */
public inline fun <T, R : Comparable<R>> Iterable<T>.maxOfOrNull(selector: (T) -> R): R? {
    val iterator = iterator()
    if (!iterator.hasNext()) return null
    var maxValue = selector(iterator.next())
    while (iterator.hasNext()) {
        val v = selector(iterator.next())
        if (maxValue < v) {
            maxValue = v
        }
    }
    return maxValue
}

/**
 * Returns the first element yielding the largest value of the given function or `null` if there are no elements.
 *
 * @sample samples.collections.Collections.Aggregates.maxByOrNull
 */
public inline fun <T, R : Comparable<R>> Iterable<T>.maxByOrNull(selector: (T) -> R): T? {
    val iterator = iterator()
    if (!iterator.hasNext()) return null
    var maxElem = iterator.next()
    if (!iterator.hasNext()) return maxElem
    var maxValue = selector(maxElem)
    do {
        val e = iterator.next()
        val v = selector(e)
        if (maxValue < v) {
            maxElem = e
            maxValue = v
        }
    } while (iterator.hasNext())
    return maxElem
}

/**
 * Returns the largest element or `null` if there are no elements.
 *
 * If any of elements is `NaN` returns `NaN`.
 */
public fun Iterable<Double>.maxOrNull(): Double? {
    val iterator = iterator()
    if (!iterator.hasNext()) return null
    var max = iterator.next()
    while (iterator.hasNext()) {
        val e = iterator.next()
        max = maxOf(max, e)
    }
    return max
}

/**
 * Returns the largest element or `null` if there are no elements.
 *
 * If any of elements is `NaN` returns `NaN`.
 */
public fun Iterable<Float>.maxOrNull(): Float? {
    val iterator = iterator()
    if (!iterator.hasNext()) return null
    var max = iterator.next()
    while (iterator.hasNext()) {
        val e = iterator.next()
        max = maxOf(max, e)
    }
    return max
}

/**
 * Returns the largest element or `null` if there are no elements.
 */
public fun <T : Comparable<T>> Iterable<T>.maxOrNull(): T? {
    val iterator = iterator()
    if (!iterator.hasNext()) return null
    var max = iterator.next()
    while (iterator.hasNext()) {
        val e = iterator.next()
        if (max < e) max = e
    }
    return max
}

/**
 * Returns the sum of all values produced by [selector] function applied to each element in the collection.
 */
public inline fun <T> Iterable<T>.sumByLong(selector: (T) -> Long): Long {
    var sum: Long = 0L
    for (element in this) {
        sum += selector(element)
    }
    return sum
}
/*** Kotlin Version Compat End ***/