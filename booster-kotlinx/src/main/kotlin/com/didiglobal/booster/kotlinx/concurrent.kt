package com.didiglobal.booster.kotlinx

import java.util.concurrent.ForkJoinPool
import java.util.concurrent.ForkJoinTask

/**
 * Execute this task
 *
 * @author johnsonlee
 */
fun <T> ForkJoinTask<T>.execute(): T {
    val pool = ForkJoinPool()
    val result = pool.invoke(this)
    pool.shutdown()
    return result
}
