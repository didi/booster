package com.didiglobal.booster.annotations

/**
 * @author johnsonlee
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class Priority(val value: Int = 0)