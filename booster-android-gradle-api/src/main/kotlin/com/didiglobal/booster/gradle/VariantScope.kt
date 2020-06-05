package com.didiglobal.booster.gradle

import com.android.build.gradle.BaseExtension
import com.android.build.gradle.internal.scope.VariantScope
import com.android.sdklib.BuildToolInfo
import java.io.File

private val EXTENSION_GETTER = when {
    GTE_V4_X -> VariantScopeV40::getExtension
    GTE_V3_6 -> VariantScopeV36::getExtension
    GTE_V3_5 -> VariantScopeV35::getExtension
    GTE_V3_3 -> VariantScopeV33::getExtension
    GTE_V3_2 -> VariantScopeV32::getExtension
    else -> VariantScopeV30::getExtension
}

private val ALL_ARTIFACTS_GETTER = when {
    GTE_V4_X -> VariantScopeV40::getAllArtifacts
    GTE_V3_6 -> VariantScopeV36::getAllArtifacts
    GTE_V3_5 -> VariantScopeV35::getAllArtifacts
    GTE_V3_3 -> VariantScopeV33::getAllArtifacts
    GTE_V3_2 -> VariantScopeV32::getAllArtifacts
    else -> VariantScopeV30::getAllArtifacts
}

private val AAR_GETTER = when {
    GTE_V4_X -> VariantScopeV40::getAar
    GTE_V3_6 -> VariantScopeV36::getAar
    GTE_V3_5 -> VariantScopeV35::getAar
    GTE_V3_3 -> VariantScopeV33::getAar
    GTE_V3_2 -> VariantScopeV32::getAar
    else -> VariantScopeV30::getAar
}

private val ALL_CLASSES_GETTER = when {
    GTE_V4_X -> VariantScopeV40::getAllClasses
    GTE_V3_6 -> VariantScopeV36::getAllClasses
    GTE_V3_5 -> VariantScopeV35::getAllClasses
    GTE_V3_3 -> VariantScopeV33::getAllClasses
    GTE_V3_2 -> VariantScopeV32::getAllClasses
    else -> VariantScopeV30::getAllClasses
}

private val APK_GETTER = when {
    GTE_V4_X -> VariantScopeV40::getApk
    GTE_V3_6 -> VariantScopeV36::getApk
    GTE_V3_5 -> VariantScopeV35::getApk
    GTE_V3_3 -> VariantScopeV33::getApk
    GTE_V3_2 -> VariantScopeV32::getApk
    else -> VariantScopeV30::getApk
}

private val JAVAC_GETTER = when {
    GTE_V4_X -> VariantScopeV40::getJavac
    GTE_V3_6 -> VariantScopeV36::getJavac
    GTE_V3_5 -> VariantScopeV35::getJavac
    GTE_V3_3 -> VariantScopeV33::getJavac
    GTE_V3_2 -> VariantScopeV32::getJavac
    else -> VariantScopeV30::getJavac
}

private val MERGED_ASSETS_GETTER = when {
    GTE_V4_X -> VariantScopeV40::getMergedAssets
    GTE_V3_6 -> VariantScopeV36::getMergedAssets
    GTE_V3_5 -> VariantScopeV35::getMergedAssets
    GTE_V3_3 -> VariantScopeV33::getMergedAssets
    GTE_V3_2 -> VariantScopeV32::getMergedAssets
    else -> VariantScopeV30::getMergedAssets
}

private val MERGED_MANIFESTS_GETTER = when {
    GTE_V4_X -> VariantScopeV40::getMergedManifests
    GTE_V3_6 -> VariantScopeV36::getMergedManifests
    GTE_V3_5 -> VariantScopeV35::getMergedManifests
    GTE_V3_3 -> VariantScopeV33::getMergedManifests
    GTE_V3_2 -> VariantScopeV32::getMergedManifests
    else -> VariantScopeV30::getMergedManifests
}

private val MERGED_RESOURCE_GETTER = when {
    GTE_V4_X -> VariantScopeV40::getMergedRes
    GTE_V3_6 -> VariantScopeV36::getMergedRes
    GTE_V3_5 -> VariantScopeV35::getMergedRes
    GTE_V3_3 -> VariantScopeV33::getMergedRes
    GTE_V3_2 -> VariantScopeV32::getMergedRes
    else -> VariantScopeV30::getMergedRes
}

private val PROCESSED_RES_GETTER = when {
    GTE_V4_X -> VariantScopeV40::getProcessedRes
    GTE_V3_6 -> VariantScopeV36::getProcessedRes
    GTE_V3_5 -> VariantScopeV35::getProcessedRes
    GTE_V3_3 -> VariantScopeV33::getProcessedRes
    GTE_V3_2 -> VariantScopeV32::getProcessedRes
    else -> VariantScopeV30::getProcessedRes
}

private val SYMBOL_LIST_GETTER = when {
    GTE_V4_X -> VariantScopeV40::getSymbolList
    GTE_V3_6 -> VariantScopeV36::getSymbolList
    GTE_V3_5 -> VariantScopeV35::getSymbolList
    GTE_V3_3 -> VariantScopeV33::getSymbolList
    GTE_V3_2 -> VariantScopeV32::getSymbolList
    else -> VariantScopeV30::getSymbolList
}

private val SYMBOL_LIST_WITH_PACKAGE_NAME_GETTER = when {
    GTE_V4_X -> VariantScopeV40::getSymbolListWithPackageName
    GTE_V3_6 -> VariantScopeV36::getSymbolListWithPackageName
    GTE_V3_5 -> VariantScopeV35::getSymbolListWithPackageName
    GTE_V3_3 -> VariantScopeV33::getSymbolListWithPackageName
    GTE_V3_2 -> VariantScopeV32::getSymbolListWithPackageName
    else -> VariantScopeV30::getSymbolListWithPackageName
}

private val BUILD_TOOLS_GETTER = when {
    GTE_V4_X -> VariantScopeV40::getBuildTools
    GTE_V3_6 -> VariantScopeV36::getBuildTools
    GTE_V3_5 -> VariantScopeV35::getBuildTools
    GTE_V3_3 -> VariantScopeV33::getBuildTools
    GTE_V3_2 -> VariantScopeV32::getBuildTools
    else -> VariantScopeV30::getBuildTools
}

private val RAW_ANDROID_RESOURCES_GETTER = when {
    GTE_V4_X -> VariantScopeV40::getRawAndroidResources
    GTE_V3_6 -> VariantScopeV36::getRawAndroidResources
    GTE_V3_5 -> VariantScopeV35::getRawAndroidResources
    GTE_V3_3 -> VariantScopeV33::getRawAndroidResources
    else -> VariantScopeV30::getRawAndroidResources
}

private val DATA_BINDING_DEPENDENCY_ARTIFACTS_GETTER = when {
    GTE_V3_2 -> VariantScopeV32::getDataBindingDependencyArtifacts
    else -> VariantScopeV30::getDataBindingDependencyArtifacts
}

val VariantScope.extension: BaseExtension
    get() = EXTENSION_GETTER(this)

val VariantScope.aar: Collection<File>
    get() = AAR_GETTER(this)

/**
 * The output directory of APK files
 */
val VariantScope.apk: Collection<File>
    get() = APK_GETTER(this)

val VariantScope.javac: Collection<File>
    get() = JAVAC_GETTER(this)

/**
 * The output directory of merged [AndroidManifest.xml](https://developer.android.com/guide/topics/manifest/manifest-intro)
 */
val VariantScope.mergedManifests: Collection<File>
    get() = MERGED_MANIFESTS_GETTER(this)

/**
 * The output directory of merged resources
 */
val VariantScope.mergedRes: Collection<File>
    get() = MERGED_RESOURCE_GETTER(this)

/**
 * The output directory of merged assets
 */
val VariantScope.mergedAssets: Collection<File>
    get() = MERGED_ASSETS_GETTER(this)

/**
 * The output directory of processed resources: *resources-**variant**.ap\_*
 */
val VariantScope.processedRes: Collection<File>
    get() = PROCESSED_RES_GETTER(this)

/**
 * All of classes
 */
val VariantScope.allClasses: Collection<File>
    get() = ALL_CLASSES_GETTER(this)

val VariantScope.symbolList: Collection<File>
    get() = SYMBOL_LIST_GETTER(this)

val VariantScope.symbolListWithPackageName: Collection<File>
    get() = SYMBOL_LIST_WITH_PACKAGE_NAME_GETTER(this)

val VariantScope.allArtifacts: Map<String, Collection<File>>
    get() = ALL_ARTIFACTS_GETTER(this)

val VariantScope.buildTools: BuildToolInfo
    get() = BUILD_TOOLS_GETTER(this)

val VariantScope.rawAndroidResources: Collection<File>
    get() = RAW_ANDROID_RESOURCES_GETTER(this)

val VariantScope.dataBindingDependencyArtifacts: File
    get() = DATA_BINDING_DEPENDENCY_ARTIFACTS_GETTER(this)