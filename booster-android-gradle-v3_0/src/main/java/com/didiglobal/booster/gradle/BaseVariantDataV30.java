package com.didiglobal.booster.gradle;

import com.android.build.gradle.internal.variant.BaseVariantData;
import com.google.wireless.android.sdk.stats.GradleBuildVariant;

class BaseVariantDataV30 {

    static boolean isForTesting(final BaseVariantData variantData) {
        return variantData.getType().isForTesting();
    }

    static boolean isAar(final BaseVariantData variantData) {
        return variantData.getType().getAnalyticsVariantType() == GradleBuildVariant.VariantType.LIBRARY;
    }

    static boolean isApk(final BaseVariantData variantData) {
        return variantData.getType().getAnalyticsVariantType() == GradleBuildVariant.VariantType.APPLICATION;
    }

    static boolean isBaseModule(final BaseVariantData variantData) {
        return false;
    }

    static boolean isDynamicFeature(final BaseVariantData variantData) {
        return false;
    }

    static boolean isHybrid(final BaseVariantData variantData) {
        return false;
    }

    static GradleBuildVariant.VariantType getAnalyticsVariantType(final BaseVariantData variantData) {
        return variantData.getType().getAnalyticsVariantType();
    }

}
