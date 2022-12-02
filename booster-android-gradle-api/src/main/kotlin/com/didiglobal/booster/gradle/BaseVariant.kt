package com.didiglobal.booster.gradle

import com.android.build.gradle.BaseExtension
import com.android.build.gradle.api.BaseVariant
import com.android.build.gradle.internal.publishing.AndroidArtifacts
import com.android.build.gradle.tasks.ProcessAndroidResources
import com.android.builder.model.ApiVersion
import com.android.sdklib.BuildToolInfo
import com.didiglobal.booster.kotlinx.file
import org.gradle.api.Incubating
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.UnknownTaskException
import org.gradle.api.artifacts.ArtifactCollection
import org.gradle.api.artifacts.result.ResolvedArtifactResult
import org.gradle.api.file.FileCollection
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.TaskProvider
import java.io.File

/**
 * The project which this variant belongs
 */
@Suppress("DEPRECATION")
val BaseVariant.project: Project
    get() = AGP.run { project }


fun BaseVariant.getReport(artifactName: String, fileName: String): File {
    return project.buildDir.file("reports", artifactName, name, fileName)
}


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
    get() = AGP.run {
        getDependencies(true)
    }

val BaseVariant.javaCompilerTaskProvider: TaskProvider<out Task>
    get() = AGP.run { javaCompilerTaskProvider }

val BaseVariant.preBuildTaskProvider: TaskProvider<out Task>
    get() = AGP.run { preBuildTaskProvider }

val BaseVariant.assembleTaskProvider: TaskProvider<out Task>
    get() = AGP.run { assembleTaskProvider }

val BaseVariant.mergeAssetsTaskProvider: TaskProvider<out Task>
    get() = AGP.run { mergeAssetsTaskProvider }

val BaseVariant.mergeResourcesTaskProvider: TaskProvider<out Task>
    get() = AGP.run { mergeResourcesTaskProvider }

val BaseVariant.mergeNativeLibsTaskProvider: TaskProvider<out Task>
    get() = AGP.run { mergeNativeLibsTaskProvider }

val BaseVariant.processJavaResourcesTaskProvider: TaskProvider<out Task>
    get() = AGP.run { processJavaResourcesTaskProvider }

val BaseVariant.processResTaskProvider: TaskProvider<out Task>?
    get() = try {
        project.tasks.named(getTaskName("process", "Resources"))
    } catch (_: UnknownTaskException) {
        null
    }

val BaseVariant.bundleResourcesTaskProvider: TaskProvider<out Task>?
    get() = try {
        project.tasks.named(getTaskName("bundle", "Resources"))
    } catch (_: UnknownTaskException) {
        null
    }

val BaseVariant.packageTaskProvider: TaskProvider<out Task>?
    get() = try {
        project.tasks.named(getTaskName("package"))
    } catch (_: UnknownTaskException) {
        null
    }

val BaseVariant.packageBundleTaskProvider: TaskProvider<out Task>?
    get() = try {
        project.tasks.named(getTaskName("package", "Bundle"))
    } catch (_: UnknownTaskException) {
        null
    }

val BaseVariant.mergeJavaResourceTaskProvider: TaskProvider<out Task>?
    get() = try {
        project.tasks.named(getTaskName("merge", "JavaResource"))
    } catch (_: UnknownTaskException) {
        null
    }

val BaseVariant.createFullJarTaskProvider: TaskProvider<out Task>?
    get() = try {
        project.tasks.named(getTaskName("createFullJar"))
    } catch (_: UnknownTaskException) {
        null
    }

val BaseVariant.bundleClassesTaskProvider: TaskProvider<out Task>?
    get() = try {
        // for AGP < 7.2.0
        project.tasks.named(getTaskName("bundle", "Classes"))
    } catch (_: UnknownTaskException) {
        // for AGP >= 7.2.0+
        try {
            project.tasks.named(getTaskName("bundle", "ClassesToRuntimeJar"))
        } catch (_: UnknownTaskException) {
            try {
                project.tasks.named(getTaskName("bundle", "ClassesToCompileJar"))
            } catch (_: UnknownTaskException) {
                null
            }
        }
    }

fun BaseVariant.getTaskName(prefix: String): String = AGP.run {
    getTaskName(prefix)
}

fun BaseVariant.getTaskName(prefix: String, suffix: String): String = AGP.run {
    getTaskName(prefix, suffix)
}

val BaseVariant.minSdkVersion: ApiVersion
    get() = AGP.run {
        minSdkVersion
    }

val BaseVariant.isApplication: Boolean
    get() = AGP.run {
        isApplication
    }

val BaseVariant.isLibrary: Boolean
    get() = AGP.run {
        isLibrary
    }

val BaseVariant.isDynamicFeature: Boolean
    get() = AGP.run {
        isDynamicFeature
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

@Incubating
fun BaseVariant.getArtifactFileCollection(
        configType: AndroidArtifacts.ConsumedConfigType,
        scope: AndroidArtifacts.ArtifactScope,
        artifactType: AndroidArtifacts.ArtifactType
): FileCollection = AGP.run {
    getArtifactFileCollection(configType, scope, artifactType)
}

val BaseVariant.aar: FileCollection
    get() = AGP.run {
        aar
    }

/**
 * The output directory of APK files
 */
val BaseVariant.apk: FileCollection
    get() = AGP.run {
        apk
    }

/**
 * The output directory of merged [AndroidManifest.xml](https://developer.android.com/guide/topics/manifest/manifest-intro)
 */
val BaseVariant.mergedManifests: FileCollection
    get() = AGP.run {
        mergedManifests
    }

/**
 * The output directory of merged resources
 */
val BaseVariant.mergedRes: FileCollection
    get() = AGP.run {
        mergedRes
    }

/**
 * The output directory of merged assets
 */
val BaseVariant.mergedAssets: FileCollection
    get() = AGP.run {
        mergedAssets
    }

/**
 * The output directory of merged native libs
 */
val BaseVariant.mergedNativeLibs: FileCollection
    get() = AGP.run {
        mergedNativeLibs
    }

/**
 * The output directory of processed resources: *resources-**variant**.ap\_*
 */
val BaseVariant.processedRes: FileCollection
    get() = AGP.run {
        processedRes
    }

/**
 * All of classes
 */
val BaseVariant.allClasses: FileCollection
    get() = AGP.run {
        allClasses
    }

val BaseVariant.symbolList: FileCollection
    get() = AGP.run {
        symbolList
    }

val BaseVariant.symbolListWithPackageName: FileCollection
    get() = AGP.run {
        symbolListWithPackageName
    }

val BaseVariant.allArtifacts: Map<String, FileCollection>
    get() = AGP.run {
        allArtifacts
    }

val BaseVariant.buildTools: BuildToolInfo
    get() = AGP.run {
        buildTools
    }

val BaseVariant.isPrecompileDependenciesResourcesEnabled: Boolean
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
val BaseVariant.javaCompilerTask: Task
    get() = AGP.run { javaCompilerTask }

/**
 * The `preBuild` task associates with this variant
 */
@Deprecated(
        message = "Use preBuildTaskProvider instead",
        replaceWith = ReplaceWith(expression = "preBuildTaskProvider"),
        level = DeprecationLevel.WARNING
)
val BaseVariant.preBuildTask: Task
    get() = AGP.run { preBuildTask }

/**
 * The `assemble` task associates with this variant
 */
@Deprecated(
        message = "Use assembleTaskProvider instead",
        replaceWith = ReplaceWith(expression = "assembleTaskProvider"),
        level = DeprecationLevel.WARNING
)
val BaseVariant.assembleTask: Task
    get() = AGP.run { assembleTask }

/**
 * The `mergeAssets` task associates with this variant
 */
@Deprecated(
        message = "Use mergeAssetsTaskProvider instead",
        replaceWith = ReplaceWith(expression = "mergeAssetsTaskProvider"),
        level = DeprecationLevel.WARNING
)
val BaseVariant.mergeAssetsTask: Task
    get() = AGP.run { mergeAssetsTask }

/**
 * The `mergeResources` task associates with this variant
 */
@Deprecated(
        message = "Use mergeResourcesTaskProvider instead",
        replaceWith = ReplaceWith(expression = "mergeResourcesTaskProvider"),
        level = DeprecationLevel.WARNING
)
val BaseVariant.mergeResourcesTask: Task
    get() = AGP.run { mergeResourcesTask }

@Deprecated(
        message = "Use processJavaResourcesTaskProvider instead",
        replaceWith = ReplaceWith(expression = "processJavaResourcesTaskProvider"),
        level = DeprecationLevel.WARNING
)
val BaseVariant.processJavaResourcesTask: Task
    get() = AGP.run { processJavaResourcesTask }

/**
 * The `processRes` task associates with this variant
 */
@Deprecated(
        message = "Use processResTaskProvider instead",
        replaceWith = ReplaceWith(expression = "processResTaskProvider"),
        level = DeprecationLevel.WARNING
)
val BaseVariant.processResTask: ProcessAndroidResources?
    get() = project.tasks.findByName(getTaskName("process", "Resources")) as? ProcessAndroidResources

/**
 * The `bundleResources` tasks associates with this variant
 */
@Deprecated(
        message = "Use bundleResourcesTaskProvider instead",
        replaceWith = ReplaceWith(expression = "bundleResourcesTaskProvider"),
        level = DeprecationLevel.WARNING
)
val BaseVariant.bundleResourcesTask: Task?
    get() = project.tasks.findByName(getTaskName("bundle", "Resources"))

/**
 * The `packageBundle` tasks associates with this variant
 */
@Deprecated(
        message = "Use packageBundleTaskProvider instead",
        replaceWith = ReplaceWith(expression = "packageBundleTaskProvider"),
        level = DeprecationLevel.WARNING
)
val BaseVariant.packageBundleTask: Task?
    get() = project.tasks.findByName(getTaskName("package", "Bundle"))

@Deprecated(
        message = "Use mergeJavaResourceTaskProvider instead",
        replaceWith = ReplaceWith(expression = "mergeJavaResourceTaskProvider"),
        level = DeprecationLevel.WARNING
)
val BaseVariant.mergeJavaResourceTask: Task?
    get() = project.tasks.findByName(getTaskName("merge", "JavaResource"))
