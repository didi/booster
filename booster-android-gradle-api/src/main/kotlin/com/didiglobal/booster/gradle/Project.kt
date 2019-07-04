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
    get() = GTE_V3_3 || ProjectV32.isAapt2Enabled(this)
