package com.didiglobal.booster.gradle

import com.android.builder.model.Version
import com.android.repository.Revision

internal val ANDROID_GRADLE_PLUGIN_VERSION = Revision.parseRevision(Version.ANDROID_GRADLE_PLUGIN_VERSION)

val GTE_V3_X = ANDROID_GRADLE_PLUGIN_VERSION.major >= 3
val GTE_V3_4 = GTE_V3_X && ANDROID_GRADLE_PLUGIN_VERSION.minor >= 4
val GTE_V3_3 = GTE_V3_X && ANDROID_GRADLE_PLUGIN_VERSION.minor >= 3
val GTE_V3_2 = GTE_V3_X && ANDROID_GRADLE_PLUGIN_VERSION.minor >= 2
val GTE_V3_1 = GTE_V3_X && ANDROID_GRADLE_PLUGIN_VERSION.minor >= 1
