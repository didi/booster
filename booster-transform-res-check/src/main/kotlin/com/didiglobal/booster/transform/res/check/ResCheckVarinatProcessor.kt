package com.didiglobal.booster.transform.res.check

import com.android.build.gradle.api.BaseVariant
import com.android.build.gradle.api.LibraryVariant
import com.didiglobal.booster.gradle.scope
import com.didiglobal.booster.task.spi.VariantProcessor
import com.google.auto.service.AutoService

/**
 * @author neighbWang
 */
@AutoService(VariantProcessor::class)
class ResCheckVarinatProcessor : VariantProcessor {

    override fun process(variant: BaseVariant) {
        if (variant !is LibraryVariant) {
            variant.scope.globalScope.project.dependencies.add("implementation", "${Build.GROUP}:booster-android-instrument-res-check:${Build.VERSION}")
        }
    }
}