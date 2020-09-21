package com.didiglobal.booster.gradle

import com.android.build.gradle.internal.variant.BaseVariantData

fun BaseVariantData.isAar() = when {
    GTE_V4_X -> BaseVariantDataV40.isAar(this)
    GTE_V3_6 -> BaseVariantDataV36.isAar(this)
    GTE_V3_5 -> BaseVariantDataV35.isAar(this)
    GTE_V3_3 -> BaseVariantDataV33.isAar(this)
    GTE_V3_2 -> BaseVariantDataV32.isAar(this)
    else -> BaseVariantDataV30.isAar(this)
}

fun BaseVariantData.isApk() = when {
    GTE_V4_X -> BaseVariantDataV40.isApk(this)
    GTE_V3_6 -> BaseVariantDataV36.isApk(this)
    GTE_V3_5 -> BaseVariantDataV35.isApk(this)
    GTE_V3_3 -> BaseVariantDataV33.isApk(this)
    GTE_V3_2 -> BaseVariantDataV32.isApk(this)
    else -> BaseVariantDataV30.isApk(this)
}

fun BaseVariantData.isBaseModule() = when {
    GTE_V4_X -> BaseVariantDataV40.isBaseModule(this)
    GTE_V3_6 -> BaseVariantDataV36.isBaseModule(this)
    GTE_V3_5 -> BaseVariantDataV35.isBaseModule(this)
    GTE_V3_3 -> BaseVariantDataV33.isBaseModule(this)
    GTE_V3_2 -> BaseVariantDataV32.isBaseModule(this)
    else -> BaseVariantDataV30.isBaseModule(this)
}

fun BaseVariantData.isDynamicFeature() = when {
    GTE_V4_X -> BaseVariantDataV40.isDynamicFeature(this)
    GTE_V3_6 -> BaseVariantDataV36.isDynamicFeature(this)
    GTE_V3_5 -> BaseVariantDataV35.isDynamicFeature(this)
    GTE_V3_3 -> BaseVariantDataV33.isDynamicFeature(this)
    GTE_V3_2 -> BaseVariantDataV32.isDynamicFeature(this)
    else -> BaseVariantDataV30.isDynamicFeature(this)
}

fun BaseVariantData.isForTesting() = when {
    GTE_V4_X -> BaseVariantDataV40.isForTesting(this)
    GTE_V3_6 -> BaseVariantDataV36.isForTesting(this)
    GTE_V3_5 -> BaseVariantDataV35.isForTesting(this)
    GTE_V3_3 -> BaseVariantDataV33.isForTesting(this)
    GTE_V3_2 -> BaseVariantDataV32.isForTesting(this)
    else -> BaseVariantDataV30.isForTesting(this)
}

fun BaseVariantData.isHybrid() = when {
    GTE_V4_X -> BaseVariantDataV40.isHybrid(this)
    GTE_V3_6 -> BaseVariantDataV36.isHybrid(this)
    GTE_V3_5 -> BaseVariantDataV35.isHybrid(this)
    GTE_V3_3 -> BaseVariantDataV33.isHybrid(this)
    GTE_V3_2 -> BaseVariantDataV32.isHybrid(this)
    else -> BaseVariantDataV30.isHybrid(this)
}

fun BaseVariantData.getAnalyticsVariantType() = when {
    GTE_V4_X -> BaseVariantDataV40.getAnalyticsVariantType(this)
    GTE_V3_6 -> BaseVariantDataV36.getAnalyticsVariantType(this)
    GTE_V3_5 -> BaseVariantDataV35.getAnalyticsVariantType(this)
    GTE_V3_3 -> BaseVariantDataV33.getAnalyticsVariantType(this)
    GTE_V3_2 -> BaseVariantDataV32.getAnalyticsVariantType(this)
    else -> BaseVariantDataV30.getAnalyticsVariantType(this)
}

fun BaseVariantData.getApplicationId() = when {
    GTE_V4_X -> BaseVariantDataV40.getApplicationId(this)
    GTE_V3_6 -> BaseVariantDataV36.getApplicationId(this)
    GTE_V3_5 -> BaseVariantDataV35.getApplicationId(this)
    GTE_V3_3 -> BaseVariantDataV33.getApplicationId(this)
    GTE_V3_2 -> BaseVariantDataV32.getApplicationId(this)
    else -> BaseVariantDataV30.getApplicationId(this)
}

fun BaseVariantData.getOriginalApplicationId() = when {
    GTE_V4_X -> BaseVariantDataV40.getOriginalApplicationId(this)
    GTE_V3_6 -> BaseVariantDataV36.getOriginalApplicationId(this)
    GTE_V3_5 -> BaseVariantDataV35.getOriginalApplicationId(this)
    GTE_V3_3 -> BaseVariantDataV33.getOriginalApplicationId(this)
    GTE_V3_2 -> BaseVariantDataV32.getOriginalApplicationId(this)
    else -> BaseVariantDataV30.getOriginalApplicationId(this)
}

