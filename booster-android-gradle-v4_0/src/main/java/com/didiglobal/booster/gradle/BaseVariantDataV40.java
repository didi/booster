package com.didiglobal.booster.gradle;

import com.android.build.gradle.internal.variant.BaseVariantData;
import com.google.wireless.android.sdk.stats.GradleBuildVariant;
import org.jetbrains.annotations.NotNull;

class BaseVariantDataV40 {

    static boolean isForTesting(final BaseVariantData variantData) {
        return variantData.getType().isForTesting();
    }

    static boolean isAar(final BaseVariantData variantData) {
        return variantData.getType().isAar();
    }

    static boolean isApk(final BaseVariantData variantData) {
        return variantData.getType().isApk();
    }

    static boolean isBaseModule(final BaseVariantData variantData) {
        return variantData.getType().isBaseModule();
    }

    static boolean isDynamicFeature(final BaseVariantData variantData) {
        return variantData.getType().isDynamicFeature();
    }

    static boolean isHybrid(final BaseVariantData variantData) {
        return false;
    }

    static GradleBuildVariant.VariantType getAnalyticsVariantType(final BaseVariantData variantData) {
        return variantData.getType().getAnalyticsVariantType();
    }

    @NotNull
    static String getApplicationId(@NotNull BaseVariantData variantData) {
        return variantData.getVariantDslInfo().getApplicationId();
    }

    static String getOriginalApplicationId(final BaseVariantData variantData) {
        return variantData.getVariantDslInfo().getOriginalApplicationId();
    }
}
