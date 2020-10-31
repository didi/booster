package com.didiglobal.booster.gradle

import com.android.build.gradle.api.BaseVariant
import com.android.build.gradle.internal.scope.GlobalScope
import com.android.build.gradle.internal.scope.VariantScope
import com.android.build.gradle.internal.variant.BaseVariantData

/**
 * The variant scope
 */
internal val BaseVariant.scope: VariantScope
    get() = AGP.run {
        variantScope
    }

@Suppress("DEPRECATION")
internal val BaseVariant.globalScope: GlobalScope
    get() = AGP.run {
        scope.globalScope
    }

/**
 * The variant data
 */
internal val BaseVariant.variantData: BaseVariantData
    get() = AGP.run {
        variantData
    }
