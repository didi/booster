package com.didiglobal.booster.transform

import com.android.SdkConstants
import com.android.build.gradle.api.BaseVariant
import com.android.build.gradle.internal.publishing.AndroidArtifacts
import com.didiglobal.booster.gradle.allClasses
import com.didiglobal.booster.gradle.apk
import com.didiglobal.booster.gradle.dataBindingDependencyArtifacts
import com.didiglobal.booster.gradle.javac
import com.didiglobal.booster.gradle.mergedAssets
import com.didiglobal.booster.gradle.mergedManifests
import com.didiglobal.booster.gradle.mergedRes
import com.didiglobal.booster.gradle.platform
import com.didiglobal.booster.gradle.processedRes
import com.didiglobal.booster.gradle.scope
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
            ArtifactManager.AAR -> scope.getArtifactCollection(AndroidArtifacts.ConsumedConfigType.RUNTIME_CLASSPATH, AndroidArtifacts.ArtifactScope.ALL, AndroidArtifacts.ArtifactType.AAR).artifactFiles.files
            ArtifactManager.ALL_CLASSES -> scope.allClasses
            ArtifactManager.APK -> scope.apk
            ArtifactManager.JAR -> scope.getArtifactCollection(AndroidArtifacts.ConsumedConfigType.RUNTIME_CLASSPATH, AndroidArtifacts.ArtifactScope.ALL, AndroidArtifacts.ArtifactType.JAR).artifactFiles.files
            ArtifactManager.JAVAC -> scope.javac
            ArtifactManager.MERGED_ASSETS -> scope.mergedAssets
            ArtifactManager.MERGED_RES -> scope.mergedRes
            ArtifactManager.MERGED_MANIFESTS -> scope.mergedManifests.search { SdkConstants.FN_ANDROID_MANIFEST_XML == it.name }
            ArtifactManager.PROCESSED_RES -> scope.processedRes.search { it.name.startsWith(SdkConstants.FN_RES_BASE) && it.name.endsWith(SdkConstants.EXT_RES) }
            ArtifactManager.SYMBOL_LIST -> scope.symbolList
            ArtifactManager.SYMBOL_LIST_WITH_PACKAGE_NAME -> scope.symbolListWithPackageName
            ArtifactManager.DATA_BINDING_DEPENDENCY_ARTIFACTS -> scope.dataBindingDependencyArtifacts.listFiles()?.toList()
                    ?: emptyList()
            else -> TODO("Unexpected type: $type")
        }

    }
