package com.didiglobal.booster.gradle

import com.android.build.gradle.internal.variant.BaseVariantData

fun BaseVariantData.isAar() = when {
    GTE_V3_2 -> BaseVariantDataV32.isAar(this)
    else -> BaseVariantDataV30.isAar(this)
}

fun BaseVariantData.isApk() = when {
    GTE_V3_2 -> BaseVariantDataV32.isApk(this)
    else -> BaseVariantDataV30.isApk(this)
}

fun BaseVariantData.isBaseModule() = when {
    GTE_V3_2 -> BaseVariantDataV32.isBaseModule(this)
    else -> BaseVariantDataV30.isBaseModule(this)
}

fun BaseVariantData.isDynamicFeature() = when {
    GTE_V3_2 -> BaseVariantDataV32.isDynamicFeature(this)
    else -> BaseVariantDataV30.isDynamicFeature(this)
}

fun BaseVariantData.isForTesting() = when {
    GTE_V3_2 -> BaseVariantDataV32.isForTesting(this)
    else -> BaseVariantDataV30.isForTesting(this)
}

fun BaseVariantData.isHybrid() = when {
    GTE_V3_2 -> BaseVariantDataV32.isHybrid(this)
    else -> BaseVariantDataV30.isHybrid(this)
}

fun BaseVariantData.getAnalyticsVariantType() = when {
    GTE_V3_2 -> BaseVariantDataV32.getAnalyticsVariantType(this)
    else -> BaseVariantDataV30.getAnalyticsVariantType(this)
}

fun BaseVariantData.getOriginalApplicationId() = when {
    GTE_V4_X -> BaseVariantDataV40.getOriginalApplicationId(this)
    else -> BaseVariantDataV30.getOriginalApplicationId(this)
}

