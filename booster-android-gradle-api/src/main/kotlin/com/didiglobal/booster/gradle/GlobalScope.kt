package com.didiglobal.booster.gradle

import com.android.build.gradle.internal.scope.GlobalScope

fun GlobalScope.hasDynamicFeatures(): Boolean = when {
    GTE_V3_2 -> GlobalScopeV32.hasDynamicFeatures(this)
    else -> false
}
