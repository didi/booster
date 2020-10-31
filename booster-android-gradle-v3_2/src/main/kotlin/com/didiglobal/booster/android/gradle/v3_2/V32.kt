package com.didiglobal.booster.android.gradle.v3_2

import com.android.build.api.artifact.ArtifactType
import com.android.build.api.artifact.BuildArtifactType
import com.android.build.api.transform.Context
import com.android.build.api.transform.QualifiedContent
import com.android.build.gradle.api.ApplicationVariant
import com.android.build.gradle.api.BaseVariant
import com.android.build.gradle.api.LibraryVariant
import com.android.build.gradle.internal.api.artifact.SourceArtifactType
import com.android.build.gradle.internal.pipeline.TransformManager
import com.android.build.gradle.internal.pipeline.TransformTask
import com.android.build.gradle.internal.publishing.AndroidArtifacts
import com.android.build.gradle.internal.scope.AnchorOutputType
import com.android.build.gradle.internal.scope.GlobalScope
import com.android.build.gradle.internal.scope.InternalArtifactType
import com.android.build.gradle.internal.scope.MissingTaskOutputException
import com.android.build.gradle.internal.scope.VariantScope
import com.android.build.gradle.internal.variant.BaseVariantData
import com.android.build.gradle.options.BooleanOption
import com.android.build.gradle.options.ProjectOptions
import com.android.build.gradle.tasks.MergeResources
import com.android.builder.core.VariantType
import com.android.builder.model.ApiVersion
import com.android.ide.common.resources.ResourceSet
import com.android.sdklib.AndroidVersion
import com.android.sdklib.BuildToolInfo
import com.didiglobal.booster.android.gradle.v3_2.V32.variantScope
import com.didiglobal.booster.gradle.AGPInterface
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.artifacts.ArtifactCollection
import java.io.File
import java.util.TreeMap

@Suppress("UnstableApiUsage")
private val ARTIFACT_TYPES = arrayOf<Array<out ArtifactType>>(
        AnchorOutputType.values(),
        BuildArtifactType.values(),
        SourceArtifactType.values(),
        InternalArtifactType.values()
).flatten().map {
    it.name() to it
}.toMap()

@Suppress("UnstableApiUsage")
private fun BaseVariant.getFinalArtifactFiles(type: ArtifactType): Collection<File> {
    return variantScope.artifacts.getFinalArtifactFiles(type).files
}

private val BaseVariant.globalScope: GlobalScope
    get() = variantScope.globalScope

object V32 : AGPInterface {

    override val scopeFullWithFeatures: MutableSet<in QualifiedContent.Scope>
        get() = TransformManager.SCOPE_FULL_WITH_FEATURES

    override val scopeFullLibraryWithFeatures: MutableSet<in QualifiedContent.Scope>
        get() = (TransformManager.SCOPE_FEATURES + QualifiedContent.Scope.PROJECT).toMutableSet()

    override val BaseVariant.project: Project
        get() = globalScope.project

    override val BaseVariant.javaCompilerTask: Task
        get() = javaCompiler

    override val BaseVariant.preBuildTask: Task
        get() = preBuild

    override val BaseVariant.assembleTask: Task
        get() = assemble

    override val BaseVariant.mergeAssetsTask: Task
        get() = mergeAssets

    override val BaseVariant.mergeResourcesTask: Task
        get() = mergeResources

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
        get() = variantData.variantConfiguration.originalApplicationId

    override val BaseVariant.hasDynamicFeature: Boolean
        get() = globalScope.hasDynamicFeatures()

    @Suppress("UNCHECKED_CAST")
    override val BaseVariant.rawAndroidResources: Collection<File>
        get() = (MergeResources::class.java.getDeclaredMethod("computeResourceSetList").apply {
            isAccessible = true
        }.invoke(mergeResources) as List<ResourceSet>).map {
            it.sourceFiles
        }.flatten().toSet()

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
                try {
                    getFinalArtifactFiles(it.value)
                } catch (e: RuntimeException) {
                    if (e.cause is MissingTaskOutputException) {
                        emptyList<File>()
                    } else {
                        throw e
                    }
                }
            }
            it.key to artifacts
        }.toMap(TreeMap())

    override val BaseVariant.minSdkVersion: AndroidVersion
        get() = variantData.variantConfiguration.minSdkVersion

    override val BaseVariant.targetSdkVersion: ApiVersion
        get() = variantData.variantConfiguration.targetSdkVersion

    override val BaseVariant.variantType: VariantType
        get() = variantScope.type

    override val BaseVariant.aar: Collection<File>
        get() = getFinalArtifactFiles(InternalArtifactType.AAR)

    override val BaseVariant.apk: Collection<File>
        get() = getFinalArtifactFiles(InternalArtifactType.APK)

    override val BaseVariant.mergedManifests: Collection<File>
        get() = getFinalArtifactFiles(InternalArtifactType.MERGED_MANIFESTS)

    override val BaseVariant.mergedRes: Collection<File>
        get() = getFinalArtifactFiles(InternalArtifactType.MERGED_RES)

    override val BaseVariant.mergedAssets: Collection<File>
        get() = getFinalArtifactFiles(when (this) {
            is ApplicationVariant -> InternalArtifactType.MERGED_ASSETS
            is LibraryVariant -> InternalArtifactType.LIBRARY_ASSETS
            else -> TODO("Unsupported variant type: $variantType")
        })

    override val BaseVariant.processedRes: Collection<File>
        get() = getFinalArtifactFiles(InternalArtifactType.PROCESSED_RES)

    override val BaseVariant.symbolList: Collection<File>
        get() = getFinalArtifactFiles(InternalArtifactType.SYMBOL_LIST)

    override val BaseVariant.symbolListWithPackageName: Collection<File>
        get() = getFinalArtifactFiles(InternalArtifactType.SYMBOL_LIST_WITH_PACKAGE_NAME)

    override val BaseVariant.dataBindingDependencyArtifacts: Collection<File>
        get() = getFinalArtifactFiles(InternalArtifactType.DATA_BINDING_DEPENDENCY_ARTIFACTS)

    override val BaseVariant.allClasses: Collection<File>
        get() = getFinalArtifactFiles(AnchorOutputType.ALL_CLASSES)

    override val BaseVariant.buildTools: BuildToolInfo
        get() = globalScope.androidBuilder.buildToolInfo

    override val Context.task: TransformTask
        get() = this as TransformTask

    override val Project.aapt2Enabled: Boolean
        get() = ProjectOptions(this).get(BooleanOption.ENABLE_AAPT2)

}
