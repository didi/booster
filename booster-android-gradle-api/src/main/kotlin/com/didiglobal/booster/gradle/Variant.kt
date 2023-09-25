package com.didiglobal.booster.gradle

import com.android.build.api.variant.AndroidVersion
import com.android.build.api.variant.Variant
import com.android.build.gradle.BaseExtension
import com.android.build.gradle.internal.SdkComponentsBuildService
import com.android.build.gradle.internal.publishing.AndroidArtifacts
import com.android.build.gradle.internal.services.getBuildService
import com.android.build.gradle.tasks.ProcessAndroidResources
import com.android.sdklib.BuildToolInfo
import com.didiglobal.booster.kotlinx.file
import org.gradle.api.Incubating
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.UnknownTaskException
import org.gradle.api.artifacts.ArtifactCollection
import org.gradle.api.artifacts.result.ResolvedArtifactResult
import org.gradle.api.file.FileCollection
import org.gradle.api.tasks.TaskProvider
import java.io.File


fun Variant.getReport(artifactName: String, fileName: String): File {
    return project.buildDir.file("reports", artifactName, name, fileName)
}

/**
 * The project which this variant belongs
 */
val Variant.project: Project
    get() = AGP.run { project }


/**
 * The `android` extension associates with this variant
 */
val Variant.extension: BaseExtension
    get() = project.getAndroid()

/**
 * The location of `$ANDROID_HOME`/platforms/android-`${compileSdkVersion}`
 */
val Variant.platform: File
    get() = getBuildService(project.gradle.sharedServices, SdkComponentsBuildService::class.java).flatMap {
        it.sdkDirectoryProvider
    }.get().asFile.resolve("platform").resolve("android-${targetVersion.apiLevel}")

/**
 * The variant dependencies
 */
val Variant.dependencies: Collection<ResolvedArtifactResult>
    get() = AGP.run {
        getDependencies(true)
    }

val Variant.targetVersion: AndroidVersion
    get() = AGP.run { targetVersion }

val Variant.javaCompilerTaskProvider: TaskProvider<out Task>
    get() = AGP.run { javaCompilerTaskProvider }

val Variant.preBuildTaskProvider: TaskProvider<out Task>
    get() = AGP.run { preBuildTaskProvider }

val Variant.assembleTaskProvider: TaskProvider<out Task>
    get() = AGP.run { assembleTaskProvider }

val Variant.mergeAssetsTaskProvider: TaskProvider<out Task>
    get() = AGP.run { mergeAssetsTaskProvider }

val Variant.mergeResourcesTaskProvider: TaskProvider<out Task>
    get() = AGP.run { mergeResourcesTaskProvider }

val Variant.mergeNativeLibsTaskProvider: TaskProvider<out Task>
    get() = AGP.run { mergeNativeLibsTaskProvider }

val Variant.processJavaResourcesTaskProvider: TaskProvider<out Task>
    get() = AGP.run { processJavaResourcesTaskProvider }

val Variant.processResTaskProvider: TaskProvider<out Task>?
    get() = try {
        project.tasks.named(getTaskName("process", "Resources"))
    } catch (_: UnknownTaskException) {
        null
    }

val Variant.bundleResourcesTaskProvider: TaskProvider<out Task>?
    get() = try {
        project.tasks.named(getTaskName("bundle", "Resources"))
    } catch (_: UnknownTaskException) {
        null
    }

val Variant.packageTaskProvider: TaskProvider<out Task>?
    get() = try {
        project.tasks.named(getTaskName("package"))
    } catch (_: UnknownTaskException) {
        null
    }

val Variant.packageBundleTaskProvider: TaskProvider<out Task>?
    get() = try {
        project.tasks.named(getTaskName("package", "Bundle"))
    } catch (_: UnknownTaskException) {
        null
    }

val Variant.mergeJavaResourceTaskProvider: TaskProvider<out Task>?
    get() = try {
        project.tasks.named(getTaskName("merge", "JavaResource"))
    } catch (_: UnknownTaskException) {
        null
    }

val Variant.createFullJarTaskProvider: TaskProvider<out Task>?
    get() = try {
        project.tasks.named(getTaskName("createFullJar"))
    } catch (_: UnknownTaskException) {
        null
    }

val Variant.bundleClassesTaskProvider: TaskProvider<out Task>?
    get() = try {
        project.tasks.named(getTaskName("bundle", "ClassesToRuntimeJar"))
    } catch (_: UnknownTaskException) {
        try {
            project.tasks.named(getTaskName("bundle", "ClassesToCompileJar"))
        } catch (_: UnknownTaskException) {
            null
        }
    }

fun Variant.getTaskName(prefix: String): String = AGP.run {
    getTaskName(prefix)
}

fun Variant.getTaskName(prefix: String, suffix: String): String = AGP.run {
    getTaskName(prefix, suffix)
}

val Variant.isApplication: Boolean
    get() = AGP.run {
        isApplication
    }

val Variant.isLibrary: Boolean
    get() = AGP.run {
        isLibrary
    }

val Variant.isDynamicFeature: Boolean
    get() = AGP.run {
        isDynamicFeature
    }

val Variant.originalApplicationId: String
    get() = AGP.run {
        originalApplicationId
    }

@Incubating
fun Variant.getArtifactCollection(
    configType: AndroidArtifacts.ConsumedConfigType,
    scope: AndroidArtifacts.ArtifactScope,
    artifactType: AndroidArtifacts.ArtifactType
): ArtifactCollection = AGP.run {
    getArtifactCollection(configType, scope, artifactType)
}

@Incubating
fun Variant.getArtifactFileCollection(
    configType: AndroidArtifacts.ConsumedConfigType,
    scope: AndroidArtifacts.ArtifactScope,
    artifactType: AndroidArtifacts.ArtifactType
): FileCollection = AGP.run {
    getArtifactFileCollection(configType, scope, artifactType)
}

val Variant.aar: FileCollection
    get() = AGP.run {
        aar
    }

val Variant.resourceSetMap: FileCollection
    get() = AGP.run {
        sourceSetMap
    }

/**
 * The output directory of APK files
 */
val Variant.apk: FileCollection
    get() = AGP.run {
        apk
    }

/**
 * The output directory of merged [AndroidManifest.xml](https://developer.android.com/guide/topics/manifest/manifest-intro)
 */
val Variant.mergedManifests: FileCollection
    get() = AGP.run {
        mergedManifests
    }

/**
 * The output directory of merged resources
 */
val Variant.mergedRes: FileCollection
    get() = AGP.run {
        mergedRes
    }

/**
 * The output directory of merged assets
 */
val Variant.mergedAssets: FileCollection
    get() = AGP.run {
        mergedAssets
    }

/**
 * The output directory of merged native libs
 */
val Variant.mergedNativeLibs: FileCollection
    get() = AGP.run {
        mergedNativeLibs
    }

/**
 * The output directory of processed resources: *resources-**variant**.ap\_*
 */
val Variant.processedRes: FileCollection
    get() = AGP.run {
        processedRes
    }

/**
 * All of classes
 */
val Variant.allClasses: FileCollection
    get() = AGP.run {
        allClasses
    }

val Variant.symbolList: FileCollection
    get() = AGP.run {
        symbolList
    }

val Variant.symbolListWithPackageName: FileCollection
    get() = AGP.run {
        symbolListWithPackageName
    }

val Variant.allArtifacts: Map<String, FileCollection>
    get() = AGP.run {
        allArtifacts
    }

val Variant.buildTools: BuildToolInfo
    get() = AGP.run {
        buildTools
    }

val Variant.isPrecompileDependenciesResourcesEnabled: Boolean
    get() = AGP.run {
        isPrecompileDependenciesResourcesEnabled
    }

/**
 * The `compileJava` task associates with this variant
 */
@Deprecated(
    message = "Use javaCompilerTaskProvider instead",
    replaceWith = ReplaceWith(expression = "javaCompilerTaskProvider"),
    level = DeprecationLevel.WARNING
)
val Variant.javaCompilerTask: Task
    get() = AGP.run { javaCompilerTask }

/**
 * The `preBuild` task associates with this variant
 */
@Deprecated(
    message = "Use preBuildTaskProvider instead",
    replaceWith = ReplaceWith(expression = "preBuildTaskProvider"),
    level = DeprecationLevel.WARNING
)
val Variant.preBuildTask: Task
    get() = AGP.run { preBuildTask }

/**
 * The `assemble` task associates with this variant
 */
@Deprecated(
    message = "Use assembleTaskProvider instead",
    replaceWith = ReplaceWith(expression = "assembleTaskProvider"),
    level = DeprecationLevel.WARNING
)
val Variant.assembleTask: Task
    get() = AGP.run { assembleTask }

/**
 * The `mergeAssets` task associates with this variant
 */
@Deprecated(
    message = "Use mergeAssetsTaskProvider instead",
    replaceWith = ReplaceWith(expression = "mergeAssetsTaskProvider"),
    level = DeprecationLevel.WARNING
)
val Variant.mergeAssetsTask: Task
    get() = AGP.run { mergeAssetsTask }

/**
 * The `mergeResources` task associates with this variant
 */
@Deprecated(
    message = "Use mergeResourcesTaskProvider instead",
    replaceWith = ReplaceWith(expression = "mergeResourcesTaskProvider"),
    level = DeprecationLevel.WARNING
)
val Variant.mergeResourcesTask: Task
    get() = AGP.run { mergeResourcesTask }

@Deprecated(
    message = "Use processJavaResourcesTaskProvider instead",
    replaceWith = ReplaceWith(expression = "processJavaResourcesTaskProvider"),
    level = DeprecationLevel.WARNING
)
val Variant.processJavaResourcesTask: Task
    get() = AGP.run { processJavaResourcesTask }

/**
 * The `processRes` task associates with this variant
 */
@Deprecated(
    message = "Use processResTaskProvider instead",
    replaceWith = ReplaceWith(expression = "processResTaskProvider"),
    level = DeprecationLevel.WARNING
)
val Variant.processResTask: ProcessAndroidResources?
    get() = project.tasks.findByName(getTaskName("process", "Resources")) as? ProcessAndroidResources

/**
 * The `bundleResources` tasks associates with this variant
 */
@Deprecated(
    message = "Use bundleResourcesTaskProvider instead",
    replaceWith = ReplaceWith(expression = "bundleResourcesTaskProvider"),
    level = DeprecationLevel.WARNING
)
val Variant.bundleResourcesTask: Task?
    get() = project.tasks.findByName(getTaskName("bundle", "Resources"))

/**
 * The `packageBundle` tasks associates with this variant
 */
@Deprecated(
    message = "Use packageBundleTaskProvider instead",
    replaceWith = ReplaceWith(expression = "packageBundleTaskProvider"),
    level = DeprecationLevel.WARNING
)
val Variant.packageBundleTask: Task?
    get() = project.tasks.findByName(getTaskName("package", "Bundle"))

@Deprecated(
    message = "Use mergeJavaResourceTaskProvider instead",
    replaceWith = ReplaceWith(expression = "mergeJavaResourceTaskProvider"),
    level = DeprecationLevel.WARNING
)
val Variant.mergeJavaResourceTask: Task?
    get() = project.tasks.findByName(getTaskName("merge", "JavaResource"))
