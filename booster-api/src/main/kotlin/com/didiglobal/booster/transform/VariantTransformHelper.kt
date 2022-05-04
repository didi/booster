package com.didiglobal.booster.transform

import com.android.SdkConstants
import com.android.build.gradle.api.BaseVariant
import com.didiglobal.booster.gradle.aar
import com.didiglobal.booster.gradle.allArtifacts
import com.didiglobal.booster.gradle.allClasses
import com.didiglobal.booster.gradle.apk
import com.didiglobal.booster.gradle.mergedAssets
import com.didiglobal.booster.gradle.mergedManifests
import com.didiglobal.booster.gradle.mergedRes
import com.didiglobal.booster.gradle.platform
import com.didiglobal.booster.gradle.processedRes
import com.didiglobal.booster.gradle.project
import com.didiglobal.booster.gradle.symbolList
import com.didiglobal.booster.gradle.symbolListWithPackageName
import com.didiglobal.booster.kotlinx.search
import com.didiglobal.booster.transform.util.TransformHelper
import java.io.File

/**
 * Represents transform helper associates with variant
 *
 * @author johnsonlee
 */
class VariantTransformHelper(variant: BaseVariant, input: File) : TransformHelper(input, variant.platform, variant.artifacts, variant.applicationId, variant.name)

val BaseVariant.artifacts: ArtifactManager
    get() = object : ArtifactManager {

        override fun get(type: String): Collection<File> = when (type) {
            ArtifactManager.AAR -> aar.files
            ArtifactManager.ALL_CLASSES -> allClasses.files
            ArtifactManager.APK -> apk.files
            ArtifactManager.MERGED_ASSETS -> mergedAssets.files
            ArtifactManager.MERGED_RES -> mergedRes.files
            ArtifactManager.MERGED_MANIFESTS -> mergedManifests.search { SdkConstants.FN_ANDROID_MANIFEST_XML == it.name }
            ArtifactManager.PROCESSED_RES -> processedRes.search { it.name.startsWith(SdkConstants.FN_RES_BASE) && it.name.endsWith(SdkConstants.EXT_RES) }
            ArtifactManager.SYMBOL_LIST -> symbolList.files
            ArtifactManager.SYMBOL_LIST_WITH_PACKAGE_NAME -> symbolListWithPackageName.files
            ArtifactManager.DATA_BINDING_DEPENDENCY_ARTIFACTS -> allArtifacts[ArtifactManager.DATA_BINDING_DEPENDENCY_ARTIFACTS]?.files ?: emptyList()
            else -> TODO("Unexpected type: $type")
        }

    }
