package com.didiglobal.booster.gradle

import com.android.builder.model.Version
import com.android.repository.Revision

internal val ANDROID_GRADLE_PLUGIN_VERSION = Revision.parseRevision(Version.ANDROID_GRADLE_PLUGIN_VERSION)

internal val GTE_V3X = ANDROID_GRADLE_PLUGIN_VERSION.major >= 3
internal val GTE_V33 = GTE_V3X && ANDROID_GRADLE_PLUGIN_VERSION.minor >= 3
internal val GTE_V32 = GTE_V3X && ANDROID_GRADLE_PLUGIN_VERSION.minor >= 2
