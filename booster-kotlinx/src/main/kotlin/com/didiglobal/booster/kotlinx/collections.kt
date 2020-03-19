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
