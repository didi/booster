package com.didiglobal.booster.task.spi

import com.android.build.api.variant.Variant
import com.android.build.api.variant.VariantBuilder
import com.android.build.gradle.api.BaseVariant

interface VariantProcessor {

    @Deprecated(
        message = "BaseVariant is deprecated,  please use process(variant: Variant) method instead",
        replaceWith = ReplaceWith(
            expression = "process(variant: Variant)"
        )
    )
    fun process(variant: BaseVariant) = Unit

    fun beforeProcess(variantBuilder: VariantBuilder) = Unit

    fun process(variant: Variant) = Unit

}
