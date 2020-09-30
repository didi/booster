package com.didiglobal.booster.gradle

import com.android.builder.model.Version
import com.android.repository.Revision
import com.didiglobal.booster.android.gradle.v3_0.V30
import com.didiglobal.booster.android.gradle.v3_2.V32
import com.didiglobal.booster.android.gradle.v3_3.V33
import com.didiglobal.booster.android.gradle.v3_5.V35
import com.didiglobal.booster.android.gradle.v3_6.V36
import com.didiglobal.booster.android.gradle.v4_0.V40
import com.didiglobal.booster.android.gradle.v4_1.V41

internal val ANDROID_GRADLE_PLUGIN_VERSION = Revision.parseRevision(Version.ANDROID_GRADLE_PLUGIN_VERSION)

val GTE_V3_X = ANDROID_GRADLE_PLUGIN_VERSION.major >= 3
val GTE_V3_6 = GTE_V3_X && ANDROID_GRADLE_PLUGIN_VERSION.minor >= 6
val GTE_V3_5 = GTE_V3_X && ANDROID_GRADLE_PLUGIN_VERSION.minor >= 5
val GTE_V3_4 = GTE_V3_X && ANDROID_GRADLE_PLUGIN_VERSION.minor >= 4
val GTE_V3_3 = GTE_V3_X && ANDROID_GRADLE_PLUGIN_VERSION.minor >= 3
val GTE_V3_2 = GTE_V3_X && ANDROID_GRADLE_PLUGIN_VERSION.minor >= 2
val GTE_V3_1 = GTE_V3_X && ANDROID_GRADLE_PLUGIN_VERSION.minor >= 1

val GTE_V4_X = ANDROID_GRADLE_PLUGIN_VERSION.major >= 4
val GTE_V4_1 = GTE_V4_X && ANDROID_GRADLE_PLUGIN_VERSION.minor >= 1

internal val AGP: AGPInterface = arrayOf(
        GTE_V4_1 to V41,
        GTE_V4_X to V40,
        GTE_V3_6 to V36,
        GTE_V3_5 to V35,
        GTE_V3_3 to V33,
        GTE_V3_2 to V32,
        GTE_V3_X to V30
).firstOrNull {
    it.first
}?.second ?: throw TODO("Incompatible with AGP $ANDROID_GRADLE_PLUGIN_VERSION")