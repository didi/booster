package com.didiglobal.booster.gradle;

import com.android.build.gradle.internal.variant.BaseVariantData;

class BaseVariantDataV40 {

    static String getOriginalApplicationId(final BaseVariantData variantData) {
        return variantData.getVariantDslInfo().getOriginalApplicationId();
    }
}
