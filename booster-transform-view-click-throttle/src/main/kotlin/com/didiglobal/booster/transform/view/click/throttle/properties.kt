package com.didiglobal.booster.transform.view.click.throttle

internal val PROPERTY_DURATION = Build.ARTIFACT.replace('-', '.') + ".duration"
internal val PROPERTY_GLOBAL = Build.ARTIFACT.replace('-', '.') + ".global"
internal val PROPERTY_IGNORES = Build.ARTIFACT.replace('-', '.') + ".ignores"
internal val PROPERTY_INCLUDES = Build.ARTIFACT.replace('-', '.') + ".includes"

internal const val DEFAULT_DURATION = 200L
internal const val DEFAULT_GLOBAL = false
