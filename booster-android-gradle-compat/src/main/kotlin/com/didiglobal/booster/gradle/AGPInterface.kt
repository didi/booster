package com.didiglobal.booster.gradle

import com.android.build.api.artifact.Artifact
import com.android.build.api.variant.AndroidComponentsExtension
import com.android.build.api.variant.AndroidVersion
import com.android.build.api.variant.Variant
import com.android.build.gradle.BaseExtension
import com.android.build.gradle.internal.publishing.AndroidArtifacts
import com.android.build.gradle.internal.variant.BaseVariantData
import com.android.builder.model.Version
import com.android.repository.Revision
import com.android.sdklib.BuildToolInfo
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.UnknownDomainObjectException
import org.gradle.api.artifacts.ArtifactCollection
import org.gradle.api.artifacts.component.ComponentIdentifier
import org.gradle.api.artifacts.result.ResolvedArtifactResult
import org.gradle.api.file.FileCollection
import org.gradle.api.file.FileSystemLocation
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.TaskProvider
import java.util.ServiceLoader

interface AGPInterface {

    val revision: Revision
        get() = REVISION

    val Variant.project: Project

    @Deprecated(
            message = "Use javaCompilerTaskProvider instead",
            replaceWith = ReplaceWith(expression = "javaCompilerTaskProvider"),
            level = DeprecationLevel.WARNING
    )
    val Variant.javaCompilerTask: Task
        get() = javaCompilerTaskProvider.get()

    @Deprecated(
            message = "Use preBuildTaskProvider instead",
            replaceWith = ReplaceWith(expression = "preBuildTaskProvider"),
            level = DeprecationLevel.WARNING
    )
    val Variant.preBuildTask: Task
        get() = preBuildTaskProvider.get()

    @Deprecated(
            message = "Use preBuildTaskProvider instead",
            replaceWith = ReplaceWith(expression = "preBuildTaskProvider"),
            level = DeprecationLevel.WARNING
    )
    val Variant.assembleTask: Task
        get() = assembleTaskProvider.get()

    @Deprecated(
            message = "Use mergeAssetsTaskProvider instead",
            replaceWith = ReplaceWith(expression = "mergeAssetsTaskProvider"),
            level = DeprecationLevel.WARNING
    )
    val Variant.mergeAssetsTask: Task
        get() = mergeAssetsTaskProvider.get()

    @Deprecated(
            message = "Use mergeResourcesTaskProvider instead",
            replaceWith = ReplaceWith(expression = "mergeResourcesTaskProvider"),
            level = DeprecationLevel.WARNING
    )
    val Variant.mergeResourcesTask: Task
        get() = mergeResourcesTaskProvider.get()


    @Deprecated(
            message = "Use processJavaResourcesTaskProvider instead",
            replaceWith = ReplaceWith(expression = "processJavaResourcesTaskProvider"),
            level = DeprecationLevel.WARNING
    )
    val Variant.processJavaResourcesTask: Task
        get() = processJavaResourcesTaskProvider.get()

    fun Variant.getTaskName(prefix: String): String

    fun Variant.getTaskName(prefix: String, suffix: String): String

    val Variant.variantData: BaseVariantData

    @Deprecated(
            message = "Use Variant.namespace instead",
            replaceWith = ReplaceWith(expression = "variant.namespace"),
    )
    val Variant.originalApplicationId: String

    val Variant.hasDynamicFeature: Boolean

    @Deprecated(message = "Deprecated, don't use it")
    val Variant.rawAndroidResources: FileCollection

    val Variant.sourceSetMap: FileCollection

    val Variant.localAndroidResources: FileCollection

    val Variant.javaCompilerTaskProvider: TaskProvider<out Task>

    val Variant.preBuildTaskProvider: TaskProvider<out Task>

    val Variant.assembleTaskProvider: TaskProvider<out Task>

    val Variant.mergeAssetsTaskProvider: TaskProvider<out Task>

    val Variant.mergeResourcesTaskProvider: TaskProvider<out Task>

    val Variant.mergeNativeLibsTaskProvider: TaskProvider<out Task>

    val Variant.processJavaResourcesTaskProvider: TaskProvider<out Task>

    fun <T : FileSystemLocation> Variant.getSingleArtifact(
            type: Artifact.Single<T>
    ): Provider<T>

    fun Variant.getArtifactCollection(
            configType: AndroidArtifacts.ConsumedConfigType,
            scope: AndroidArtifacts.ArtifactScope,
            artifactType: AndroidArtifacts.ArtifactType
    ): ArtifactCollection

    fun Variant.getArtifactFileCollection(
            configType: AndroidArtifacts.ConsumedConfigType,
            scope: AndroidArtifacts.ArtifactScope,
            artifactType: AndroidArtifacts.ArtifactType
    ): FileCollection

    val Variant.allArtifacts: Map<String, FileCollection>

    val Variant.targetVersion: AndroidVersion

    val Variant.isApplication: Boolean

    val Variant.isLibrary: Boolean

    val Variant.isDynamicFeature: Boolean

    val Variant.aar: FileCollection

    val Variant.apk: FileCollection

    val Variant.mergedManifests: FileCollection

    val Variant.mergedRes: FileCollection

    val Variant.mergedAssets: FileCollection

    val Variant.mergedNativeLibs: FileCollection

    val Variant.processedRes: FileCollection

    val Variant.symbolList: FileCollection

    val Variant.symbolListWithPackageName: FileCollection

    val Variant.dataBindingDependencyArtifacts: FileCollection

    val Variant.allClasses: FileCollection

    val Variant.buildTools: BuildToolInfo

    val Variant.isPrecompileDependenciesResourcesEnabled: Boolean

    val Variant.isDebuggable: Boolean

    fun Variant.getDependencies(
            transitive: Boolean = true,
            filter: (ComponentIdentifier) -> Boolean = { true }
    ): Collection<ResolvedArtifactResult>

}

@Deprecated("Deprecated", ReplaceWith("getAndroidComponent"))
inline fun <reified T : BaseExtension> Project.getAndroid(): T = extensions.getByName("android") as T

@Deprecated("Deprecated", ReplaceWith("getAndroidComponent"))
inline fun <reified T : BaseExtension> Project.getAndroidOrNull(): T? = try {
    extensions.getByName("android") as? T
} catch (e: UnknownDomainObjectException) {
    null
}

inline fun <reified T : AndroidComponentsExtension<*, *, *>> Project.getAndroidComponents() = extensions.getByType(AndroidComponentsExtension::class.java) as T

inline fun <reified T : AndroidComponentsExtension<*, *, *>> Project.getAndroidComponentsOrNull() = try {
    getAndroidComponents<T>()
} catch (e: UnknownDomainObjectException) {
    null
}

private val REVISION: Revision by lazy {
    Revision.parseRevision(Version.ANDROID_GRADLE_PLUGIN_VERSION)
}

private val FACTORIES: List<AGPInterfaceFactory> by lazy {
    ServiceLoader.load(AGPInterfaceFactory::class.java, AGPInterface::class.java.classLoader)
            .sortedByDescending(AGPInterfaceFactory::revision)
            .toList()
}

val AGP: AGPInterface by lazy {
    val factory = FACTORIES.firstOrNull {
        it.revision.major == REVISION.major && it.revision.minor == REVISION.minor
    } ?: FACTORIES.firstOrNull {
        it.revision.major == REVISION.major
    } ?: FACTORIES.first()
    factory.newAGPInterface()
}
