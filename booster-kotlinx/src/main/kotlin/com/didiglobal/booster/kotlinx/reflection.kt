@file:Suppress("UNCHECKED_CAST")

package com.didiglobal.booster.kotlinx

import java.lang.reflect.Field
import java.lang.reflect.Method

/**
 * @author neighbWang
 */

operator fun Any.get(name: String): Any? = when (this) {
    is Class<*> -> try {
        getField(this, name)?.apply {
            isAccessible = true
        }?.get(null)
    } catch (e: NoSuchFieldException) {
        null
    }
    else -> try {
        getField(this.javaClass, name)?.apply {
            isAccessible = true
        }?.get(this)
    } catch (e: NoSuchFieldException) {
        null
    }
}

fun <T> Any.call(name: String, types: Array<Class<*>> = emptyArray(), args: Array<Any> = emptyArray()): T? = when (this) {
    is Class<*> -> try {
        getMethod(this, name, *types)?.apply {
            isAccessible = true
        }?.invoke(null, *args) as T
    } catch (e: NoSuchMethodException) {
        null
    }
    else -> try {
        getMethod(this.javaClass, name, *types)?.apply {
            isAccessible = true
        }?.invoke(this, *args) as T
    } catch (e: NoSuchMethodException) {
        null
    }
}

fun <T> Any.call(name: String, vararg args: Pair<Class<*>, Any>): T? {
    return this.call(
            name,
            args.map(Pair<Class<*>, Any>::first).toTypedArray(),
            args.map(Pair<Class<*>, Any>::second).toTypedArray()
    )
}

private fun getField(clazz: Class<*>, name: String): Field? = try {
    clazz.getDeclaredField(name)
} catch (e: NoSuchFieldException) {
    clazz.superclass?.let {
        getField(it, name)
    }
}

private fun getMethod(clazz: Class<*>, name: String, vararg args: Class<*>): Method? = try {
    clazz.getDeclaredMethod(name, *args)
} catch (e: NoSuchMethodException) {
    clazz.superclass?.let {
        getMethod(it, name, *args)
    }
}
