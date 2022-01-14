package com.didiglobal.booster.transform

/**
 * A collector is used for collecting information from the transform pipeline,
 * it's a complementary of [Transformer] which is a one-way pipeline
 */
interface Collector<R> {
    /**
     * Determine the input is acceptable by this collector
     */
    fun accept(name: String): Boolean

    /**
     * Returns the collected result, a non-null result means the matched input is out-of-date.
     */
    fun collect(name: String, data: () -> ByteArray): R
}

/**
 * A supervisor is used for observing information from the transform pipeline,
 * and it never causes the input out-of-date
 */
interface Supervisor : Collector<Unit>
