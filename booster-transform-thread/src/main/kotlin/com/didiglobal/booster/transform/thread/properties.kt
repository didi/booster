package com.didiglobal.booster.transform.thread

internal val PROPERTY_OPTIMIZATION_ENABLED = Build.ARTIFACT.replace('-', '.') + ".optimization.enabled"

/**
 * Thread optimization is enabled in default (to compatible with older versions)
 */
internal const val DEFAULT_OPTIMIZATION_ENABLED = true
