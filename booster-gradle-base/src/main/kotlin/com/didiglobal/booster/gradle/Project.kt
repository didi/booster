package com.didiglobal.booster.gradle

import com.android.build.gradle.BaseExtension
import org.gradle.api.Project

/**
 * Returns android extension
 *
 * @author johnsonlee
 */
inline fun <reified T : BaseExtension> Project.getAndroid(): T {
    return extensions.getByName("android") as T
}
