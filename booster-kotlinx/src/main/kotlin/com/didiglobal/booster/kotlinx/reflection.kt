@file:Suppress("UNCHECKED_CAST")

package com.didiglobal.booster.kotlinx

import java.lang.reflect.Field
import java.lang.reflect.Method

/**
 * @author neighbWang
 */

inline fun <reified T> Any.getStaticFieldValue(clazz: Class<*>, name: String) = getField(javaClass, name)?.get(this) as T

inline fun <reified T> Any.setStaticFieldValue(clazz: Class<*>, name: String, value: Any) = getField(javaClass, name)?.set(this, value)

inline fun <reified T> Any.getFieldValue(name: String) = getField(javaClass, name)?.get(this)

inline fun <reified T> Any.getFieldValue(type: Class<*>) = getField(javaClass, type)?.get(this)

inline fun <reified T> Any.seFieldValue(name: String, value: Any) = getField(javaClass, name)?.set(this, value)

fun Any.getField(clazz: Class<*>?, type: Class<*>): Field? = clazz?.declaredFields?.let { array ->
    return if (array.isNotEmpty()) array.asSequence().first { it.type == type } else getField(clazz.superclass, type)
}

fun getField(clazz: Class<*>?, name: String): Field? = try {
    clazz?.getDeclaredField(name)?.apply {
        isAccessible = true
    }
} catch (e: NoSuchFieldException) {
    getField(clazz?.superclass, name)
}

inline fun <reified T> invokeStaticMethod(clazz: Class<*>, name: String): T? = invokeStaticMethod(clazz, name, arrayOfNulls(0), arrayOfNulls(0))

inline fun <reified T> invokeStaticMethod(clazz: Class<*>, name: String, types: Array<Class<*>?>, args: Array<Any?>): T? = run {
    assert(types.size == args.size)
    getMethod(clazz, name, types)?.apply {
        isAccessible = true
    }?.invoke(clazz, *args) as T
}

inline fun <reified T> invokeMethod(obj: Any, name: String): T? = invokeMethod(obj, name, arrayOfNulls(0), arrayOfNulls(0))

inline fun <reified T> invokeMethod(obj: Any, name: String, types: Array<Class<*>?>, args: Array<Any?>): T? = run {
    assert(types.size == args.size)
    getMethod(obj.javaClass, name, types)?.apply {
        isAccessible = true
    }?.invoke(obj, *args) as T
}

fun getMethod(clazz: Class<*>?, name: String, types: Array<Class<*>?>): Method? = try {
    clazz?.getDeclaredMethod(name, *types)?.apply {
        isAccessible = true
    }
} catch (e: NoSuchMethodException) {
    getMethod(clazz?.superclass, name, types)
}

