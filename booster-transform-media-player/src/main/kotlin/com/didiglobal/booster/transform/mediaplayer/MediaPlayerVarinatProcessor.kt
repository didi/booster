package com.didiglobal.booster.transform.mediaplayer

import com.android.build.gradle.api.BaseVariant
import com.android.build.gradle.api.LibraryVariant
import com.didiglobal.booster.gradle.project
import com.didiglobal.booster.gradle.variantType
import com.didiglobal.booster.task.spi.VariantProcessor
import com.didiglobal.booster.transform.media.player.Build
import com.google.auto.service.AutoService

/**
 * @author neighbWang
 */
@AutoService(VariantProcessor::class)
class MediaPlayerVarinatProcessor : VariantProcessor {

    override fun process(variant: BaseVariant) {
        if (variant !is LibraryVariant && !variant.variantType.isDynamicFeature) {
            variant.project.dependencies.add("implementation", "${Build.GROUP}:booster-android-instrument-media-player:${Build.VERSION}")
        }
    }

}
