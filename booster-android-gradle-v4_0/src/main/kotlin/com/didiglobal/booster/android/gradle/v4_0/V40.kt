package com.didiglobal.booster.android.gradle.v4_0

import com.android.build.api.transform.Context
import com.android.build.api.transform.QualifiedContent
import com.android.build.gradle.api.ApplicationVariant
import com.android.build.gradle.api.BaseVariant
import com.android.build.gradle.api.LibraryVariant
import com.android.build.gradle.internal.pipeline.TransformManager
import com.android.build.gradle.internal.pipeline.TransformTask
import com.android.build.gradle.internal.publishing.AndroidArtifacts
import com.android.build.gradle.internal.scope.AnchorOutputType
import com.android.build.gradle.internal.scope.GlobalScope
import com.android.build.gradle.internal.scope.InternalArtifactType
import com.android.build.gradle.internal.scope.SingleArtifactType
import com.android.build.gradle.internal.scope.VariantScope
import com.android.build.gradle.internal.variant.BaseVariantData
import com.android.builder.core.VariantType
import com.android.builder.model.ApiVersion
import com.android.sdklib.AndroidVersion
import com.android.sdklib.BuildToolInfo
import com.didiglobal.booster.android.gradle.v4_0.V40.variantScope
import com.didiglobal.booster.gradle.AGPInterface
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.artifacts.ArtifactCollection
import org.gradle.api.file.FileSystemLocation
import java.io.File
import java.util.TreeMap

@Suppress("UnstableApiUsage")
internal val ARTIFACT_TYPES = arrayOf(
        AnchorOutputType::class,
        InternalArtifactType::class
).map {
    it.sealedSubclasses
}.flatten().map {
    it.objectInstance as SingleArtifactType<out FileSystemLocation>
}.map {
    it.javaClass.simpleName to it
}.toMap()

@Suppress("UnstableApiUsage")
private fun <T : FileSystemLocation> BaseVariant.getFinalArtifactFiles(type: SingleArtifactType<T>): Collection<File> {
    return listOfNotNull(variantScope.artifacts.getFinalProduct(type).map(FileSystemLocation::getAsFile).orNull)
}

private val BaseVariant.globalScope: GlobalScope
    get() = variantScope.globalScope


object V40 : AGPInterface {

    override val scopeFullWithFeatures: MutableSet<in QualifiedContent.Scope>
        get() = TransformManager.SCOPE_FULL_WITH_FEATURES

    override val scopeFullLibraryWithFeatures: MutableSet<in QualifiedContent.Scope>
        get() = (TransformManager.SCOPE_FEATURES + QualifiedContent.Scope.PROJECT).toMutableSet()

    override val BaseVariant.project: Project
        get() = globalScope.project

    override val BaseVariant.javaCompilerTask: Task
        get() = javaCompileProvider.get()

    override val BaseVariant.preBuildTask: Task
        get() = preBuildProvider.get()

    override val BaseVariant.assembleTask: Task
        get() = assembleProvider.get()

    override val BaseVariant.mergeAssetsTask: Task
        get() = mergeAssetsProvider.get()

    override val BaseVariant.mergeResourcesTask: Task
        get() = mergeResourcesProvider.get()

    override fun BaseVariant.getTaskName(prefix: String): String {
        return variantScope.getTaskName(prefix)
    }

    override fun BaseVariant.getTaskName(prefix: String, suffix: String): String {
        return variantScope.getTaskName(prefix, suffix)
    }

    override val BaseVariant.variantData: BaseVariantData
        get() = javaClass.getDeclaredMethod("getVariantData").apply {
            isAccessible = true
        }.invoke(this) as BaseVariantData

    override val BaseVariant.variantScope: VariantScope
        get() = variantData.scope

    override val BaseVariant.globalScope: GlobalScope
        get() = variantScope.globalScope

    override val BaseVariant.originalApplicationId: String
        get() = variantData.variantDslInfo.originalApplicationId

    override val BaseVariant.hasDynamicFeature: Boolean
        get() = globalScope.hasDynamicFeatures()

    override val BaseVariant.rawAndroidResources: Collection<File>
        get() = variantData.allRawAndroidResources.files

    override fun BaseVariant.getArtifactCollection(
            configType: AndroidArtifacts.ConsumedConfigType,
            scope: AndroidArtifacts.ArtifactScope,
            artifactType: AndroidArtifacts.ArtifactType
    ): ArtifactCollection {
        return variantScope.getArtifactCollection(configType, scope, artifactType)
    }

    override val BaseVariant.allArtifacts: Map<String, Collection<File>>
        get() = ARTIFACT_TYPES.entries.map {
            val artifacts: Collection<File> by lazy {
                getFinalArtifactFiles(it.value)
            }
            it.key to artifacts
        }.toMap(TreeMap())

    override val BaseVariant.minSdkVersion: AndroidVersion
        get() = variantData.variantDslInfo.minSdkVersion

    override val BaseVariant.targetSdkVersion: ApiVersion
        get() = variantData.variantDslInfo.targetSdkVersion

    override val BaseVariant.variantType: VariantType
        get() = variantData.variantDslInfo.variantType

    override val BaseVariant.aar: Collection<File>
        get() = getFinalArtifactFiles(InternalArtifactType.AAR)

    override val BaseVariant.apk: Collection<File>
        get() = getFinalArtifactFiles(InternalArtifactType.APK)

    override val BaseVariant.mergedManifests: Collection<File>
        get() = when (this) {
            is ApplicationVariant -> getFinalArtifactFiles(InternalArtifactType.MERGED_MANIFESTS)
            is LibraryVariant -> getFinalArtifactFiles(InternalArtifactType.LIBRARY_MANIFEST)
            else -> TODO("Unsupported variant type: $variantType")
        }

    override val BaseVariant.mergedRes: Collection<File>
        get() = getFinalArtifactFiles(InternalArtifactType.MERGED_RES)

    override val BaseVariant.mergedAssets: Collection<File>
        get() = when (this) {
            is ApplicationVariant -> getFinalArtifactFiles(InternalArtifactType.MERGED_ASSETS)
            is LibraryVariant -> getFinalArtifactFiles(InternalArtifactType.LIBRARY_ASSETS)
            else -> TODO("Unsupported variant type: $variantType")
        }

    override val BaseVariant.processedRes: Collection<File>
        get() = getFinalArtifactFiles(InternalArtifactType.PROCESSED_RES)

    override val BaseVariant.symbolList: Collection<File>
        get() = when (this) {
            is ApplicationVariant -> getFinalArtifactFiles(InternalArtifactType.RUNTIME_SYMBOL_LIST)
            is LibraryVariant -> getFinalArtifactFiles(InternalArtifactType.COMPILE_SYMBOL_LIST)
            else -> TODO("Unsupported variant type : $variantType")
        }

    override val BaseVariant.symbolListWithPackageName: Collection<File>
        get() = getFinalArtifactFiles(InternalArtifactType.SYMBOL_LIST_WITH_PACKAGE_NAME)

    override val BaseVariant.dataBindingDependencyArtifacts: Collection<File>
        get() = getFinalArtifactFiles(InternalArtifactType.DATA_BINDING_DEPENDENCY_ARTIFACTS)

    override val BaseVariant.allClasses: Collection<File>
        get() = getFinalArtifactFiles(InternalArtifactType.JAVAC)

    override val BaseVariant.buildTools: BuildToolInfo
        get() = globalScope.sdkComponents.buildToolInfoProvider.get()

    override val Context.task: TransformTask
        get() = javaClass.getDeclaredField("this$1").apply {
            isAccessible = true
        }.get(this).run {
            javaClass.getDeclaredField("this$0").apply {
                isAccessible = true
            }.get(this)
        } as TransformTask

    override val Project.aapt2Enabled: Boolean
        get() = true

}
