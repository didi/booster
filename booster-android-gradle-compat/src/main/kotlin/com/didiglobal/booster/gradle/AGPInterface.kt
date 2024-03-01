package com.didiglobal.booster.gradle

import com.android.build.api.artifact.Artifact
import com.android.build.api.dsl.CommonExtension
import com.android.build.api.variant.AndroidComponentsExtension
import com.android.build.api.variant.AndroidVersion
import com.android.build.api.variant.Variant
import com.android.build.api.variant.VariantBuilder
import com.android.build.gradle.BaseExtension
import com.android.build.gradle.internal.publishing.AndroidArtifacts
import com.android.build.gradle.internal.variant.BaseVariantData
import com.android.builder.model.Version
import com.android.repository.Revision
import com.android.sdklib.BuildToolInfo
import org.gradle.api.Project
import org.gradle.api.UnknownDomainObjectException
import org.gradle.api.artifacts.ArtifactCollection
import org.gradle.api.artifacts.component.ComponentIdentifier
import org.gradle.api.artifacts.result.ResolvedArtifactResult
import org.gradle.api.file.FileCollection
import org.gradle.api.file.FileSystemLocation
import org.gradle.api.provider.Provider
import java.util.ServiceLoader

interface AGPInterface {

    val revision: Revision
        get() = REVISION

    val Variant.project: Project

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

inline fun <reified T : AndroidComponentsExtension<out CommonExtension<*, *, *, *>, out VariantBuilder, out Variant>> Project.getAndroidComponents() = extensions.getByName("androidComponents") as T

inline fun <reified T : AndroidComponentsExtension<out CommonExtension<*, *, *, *>, out VariantBuilder, out Variant>> Project.getAndroidComponentsOrNull() = try {
    extensions.getByName("androidComponents") as T
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
