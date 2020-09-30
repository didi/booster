package com.didiglobal.booster.gradle

import com.android.build.gradle.BaseExtension
import com.android.repository.Revision
import org.gradle.api.Project

/**
 * Returns android extension
 *
 * @author johnsonlee
 */
inline fun <reified T : BaseExtension> Project.getAndroid(): T = extensions.getByName("android") as T

/**
 * The gradle version
 */
val Project.gradleVersion: Revision
    get() = Revision.parseRevision(gradle.gradleVersion)

val Project.aapt2Enabled: Boolean
    get() = AGP.run { aapt2Enabled }

inline fun <reified T> Project.getProperty(name: String, defaultValue: T): T {
    val value = findProperty(name) ?: return defaultValue
    return when (defaultValue) {
        is Boolean -> if (value is Boolean) value as T else value.toString().toBoolean() as T
        is Byte -> if (value is Byte) value as T else value.toString().toByte() as T
        is Short -> if (value is Short) value as T else value.toString().toShort() as T
        is Int -> if (value is Int) value as T else value.toString().toInt() as T
        is Float -> if (value is Float) value as T else value.toString().toFloat() as T
        is Long -> if (value is Long) value as T else value.toString().toLong() as T
        is Double -> if (value is Double) value as T else value.toString().toDouble() as T
        is String -> if (value is String) value as T else value.toString() as T
        else -> value as T
    }
}
