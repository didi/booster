package com.didiglobal.booster.transform.finalizer.watchdog.daemon

import com.android.build.api.variant.Variant
import com.didiglobal.booster.gradle.isDynamicFeature
import com.didiglobal.booster.gradle.isLibrary
import com.didiglobal.booster.gradle.project
import com.didiglobal.booster.task.spi.VariantProcessor
import com.didiglobal.booster.transform.finalizer.watchdog.daemon.Build.*
import com.google.auto.service.AutoService

/**
 * @author neighbWang
 */
@AutoService(VariantProcessor::class)
class FinalizerWatchdogDaemonVariantProcessor : VariantProcessor {

    override fun process(variant: Variant) {
        if (!variant.isLibrary && !variant.isDynamicFeature) {
            variant.project.dependencies.add("implementation", "$GROUP:booster-android-instrument-finalizer-watchdog-daemon:$VERSION")
        }
    }

}
