package com.didiglobal.booster.transform.thread

import com.android.build.gradle.api.BaseVariant
import com.android.build.gradle.api.LibraryVariant
import com.didiglobal.booster.gradle.scope
import com.didiglobal.booster.task.spi.VariantProcessor
import com.google.auto.service.AutoService

@AutoService(VariantProcessor::class)
class ThreadVariantProcessor : VariantProcessor {

    override fun process(variant: BaseVariant) {
        if (variant !is LibraryVariant) {
            variant.scope.globalScope.project.dependencies.add("implementation", "${Build.GROUP}:booster-android-instrument-thread:${Build.VERSION}")
        }
    }

}
