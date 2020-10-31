package com.didiglobal.booster.gradle

import com.android.build.gradle.BaseExtension
import com.android.build.gradle.api.BaseVariant
import com.android.build.gradle.internal.publishing.AndroidArtifacts
import com.android.build.gradle.tasks.ProcessAndroidResources
import com.android.builder.core.VariantType
import com.android.sdklib.AndroidVersion
import com.android.sdklib.BuildToolInfo
import org.gradle.api.Incubating
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.artifacts.ArtifactCollection
import org.gradle.api.artifacts.result.ResolvedArtifactResult
import java.io.File

/**
 * The project which this variant belongs
 */
@Suppress("DEPRECATION")
val BaseVariant.project: Project
    get() = AGP.run { globalScope }.project

/**
 * The `android` extension associates with this variant
 */
val BaseVariant.extension: BaseExtension
    get() = project.getAndroid()

/**
 * The location of `$ANDROID_HOME`/platforms/android-`${compileSdkVersion}`
 */
val BaseVariant.platform: File
    get() = extension.run {
        sdkDirectory.resolve("platforms").resolve(compileSdkVersion!!)
    }

/**
 * The variant dependencies
 */
val BaseVariant.dependencies: Collection<ResolvedArtifactResult>
    get() = ResolvedArtifactResults(this)

/**
 * The `compileJava` task associates with this variant
 */
@Suppress("DEPRECATION")
val BaseVariant.javaCompilerTask: Task
    get() = AGP.run { javaCompilerTask }

/**
 * The `preBuild` task associates with this variant
 */
@Suppress("DEPRECATION")
val BaseVariant.preBuildTask: Task
    get() = AGP.run { preBuildTask }

/**
 * The `assemble` task associates with this variant
 */
@Suppress("DEPRECATION")
val BaseVariant.assembleTask: Task
    get() = AGP.run { assembleTask }

/**
 * The `mergeAssets` task associates with this variant
 */
@Suppress("DEPRECATION")
val BaseVariant.mergeAssetsTask: Task
    get() = AGP.run { mergeAssetsTask }

/**
 * The `mergeResources` task associates with this variant
 */
@Suppress("DEPRECATION")
val BaseVariant.mergeResourcesTask: Task
    get() = AGP.run { mergeResourcesTask }

/**
 * The `processRes` task associates with this variant
 */
val BaseVariant.processResTask: ProcessAndroidResources
    get() = project.tasks.findByName(getTaskName("process", "Resources")) as ProcessAndroidResources

/**
 * The `bundleResources` tasks associates with this variant
 */
val BaseVariant.bundleResourcesTask: Task?
    get() = project.tasks.findByName(getTaskName("bundle", "Resources"))

/**
 * The `packageBundle` tasks associates with this variant
 */
val BaseVariant.packageBundleTask: Task?
    get() = project.tasks.findByName(getTaskName("package", "Bundle"))

fun BaseVariant.getTaskName(prefix: String): String = AGP.run {
    getTaskName(prefix)
}

fun BaseVariant.getTaskName(prefix: String, suffix: String): String = AGP.run {
    getTaskName(prefix, suffix)
}

val BaseVariant.minSdkVersion: AndroidVersion
    get() = AGP.run {
        minSdkVersion
    }

val BaseVariant.variantType: VariantType
    get() = AGP.run {
        variantType
    }

val BaseVariant.originalApplicationId: String
    get() = AGP.run {
        originalApplicationId
    }

@Incubating
fun BaseVariant.getArtifactCollection(
        configType: AndroidArtifacts.ConsumedConfigType,
        scope: AndroidArtifacts.ArtifactScope,
        artifactType: AndroidArtifacts.ArtifactType
): ArtifactCollection = AGP.run {
    getArtifactCollection(configType, scope, artifactType)
}

val BaseVariant.aar: Collection<File>
    get() = AGP.run {
        aar
    }

/**
 * The output directory of APK files
 */
val BaseVariant.apk: Collection<File>
    get() = AGP.run {
        apk
    }

/**
 * The output directory of merged [AndroidManifest.xml](https://developer.android.com/guide/topics/manifest/manifest-intro)
 */
val BaseVariant.mergedManifests: Collection<File>
    get() = AGP.run {
        mergedManifests
    }

/**
 * The output directory of merged resources
 */
val BaseVariant.mergedRes: Collection<File>
    get() = AGP.run {
        mergedRes
    }

/**
 * The output directory of merged assets
 */
val BaseVariant.mergedAssets: Collection<File>
    get() = AGP.run {
        mergedAssets
    }

/**
 * The output directory of processed resources: *resources-**variant**.ap\_*
 */
val BaseVariant.processedRes: Collection<File>
    get() = AGP.run {
        processedRes
    }

/**
 * All of classes
 */
val BaseVariant.allClasses: Collection<File>
    get() = AGP.run {
        allClasses
    }

val BaseVariant.symbolList: Collection<File>
    get() = AGP.run {
        symbolList
    }

val BaseVariant.symbolListWithPackageName: Collection<File>
    get() = AGP.run {
        symbolListWithPackageName
    }

val BaseVariant.allArtifacts: Map<String, Collection<File>>
    get() = AGP.run {
        allArtifacts
    }

val BaseVariant.buildTools: BuildToolInfo
    get() = AGP.run {
        buildTools
    }
