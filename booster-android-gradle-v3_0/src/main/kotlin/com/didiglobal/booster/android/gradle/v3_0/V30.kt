package com.didiglobal.booster.android.gradle.v3_0

import com.android.build.api.transform.Context
import com.android.build.gradle.api.ApplicationVariant
import com.android.build.gradle.api.BaseVariant
import com.android.build.gradle.api.LibraryVariant
import com.android.build.gradle.internal.pipeline.TransformTask
import com.android.build.gradle.internal.publishing.AndroidArtifacts
import com.android.build.gradle.internal.scope.GlobalScope
import com.android.build.gradle.internal.scope.MissingTaskOutputException
import com.android.build.gradle.internal.scope.TaskOutputHolder
import com.android.build.gradle.internal.scope.VariantScope
import com.android.build.gradle.internal.variant.BaseVariantData
import com.android.build.gradle.options.BooleanOption
import com.android.build.gradle.options.ProjectOptions
import com.android.build.gradle.tasks.MergeResources
import com.android.builder.core.VariantType
import com.android.builder.model.ApiVersion
import com.android.ide.common.res2.ResourceSet
import com.android.sdklib.AndroidVersion
import com.android.sdklib.BuildToolInfo
import com.didiglobal.booster.android.gradle.v3_0.V30.variantScope
import com.didiglobal.booster.gradle.AGPInterface
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.artifacts.ArtifactCollection
import java.io.File
import java.util.TreeMap

@Suppress("UnstableApiUsage")
private val ARTIFACT_TYPES = arrayOf<Array<out TaskOutputHolder.OutputType>>(
        TaskOutputHolder.TaskOutputType.values(),
        TaskOutputHolder.AnchorOutputType.values()
).flatten().map {
    it.name() to it
}.toMap()

@Suppress("UnstableApiUsage")
private fun BaseVariant.getFinalArtifactFiles(type: TaskOutputHolder.OutputType): Collection<File> {
    return variantScope.getOutput(type).files
}

private val BaseVariant.globalScope: GlobalScope
    get() = variantScope.globalScope

object V30 : AGPInterface {

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
        get() = false

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
        get() = variantData.type

    override val BaseVariant.aar: Collection<File>
        get() = getFinalArtifactFiles(TaskOutputHolder.TaskOutputType.AAR)

    override val BaseVariant.apk: Collection<File>
        get() = getFinalArtifactFiles(TaskOutputHolder.TaskOutputType.APK)

    override val BaseVariant.mergedManifests: Collection<File>
        get() = getFinalArtifactFiles(when (this) {
            is ApplicationVariant -> TaskOutputHolder.TaskOutputType.MERGED_MANIFESTS
            is LibraryVariant -> TaskOutputHolder.TaskOutputType.LIBRARY_MANIFEST
            else -> TODO("Unsupported variant type: $variantType")
        })

    override val BaseVariant.mergedRes: Collection<File>
        get() = getFinalArtifactFiles(TaskOutputHolder.TaskOutputType.MERGED_RES)

    override val BaseVariant.mergedAssets: Collection<File>
        get() = getFinalArtifactFiles(TaskOutputHolder.TaskOutputType.MERGED_ASSETS)

    override val BaseVariant.processedRes: Collection<File>
        get() = getFinalArtifactFiles(TaskOutputHolder.TaskOutputType.PROCESSED_RES)

    override val BaseVariant.symbolList: Collection<File>
        get() = getFinalArtifactFiles(TaskOutputHolder.TaskOutputType.SYMBOL_LIST)

    override val BaseVariant.symbolListWithPackageName: Collection<File>
        get() = getFinalArtifactFiles(TaskOutputHolder.TaskOutputType.SYMBOL_LIST_WITH_PACKAGE_NAME)

    override val BaseVariant.dataBindingDependencyArtifacts: Collection<File>
        get() = getFinalArtifactFiles(TaskOutputHolder.TaskOutputType.DATA_BINDING_DEPENDENCY_ARTIFACTS)

    override val BaseVariant.allClasses: Collection<File>
        get() = getFinalArtifactFiles(TaskOutputHolder.AnchorOutputType.ALL_CLASSES)

    override val BaseVariant.buildTools: BuildToolInfo
        get() = globalScope.androidBuilder.buildToolInfo

    override val Context.task: TransformTask
        get() = this as TransformTask

    override val Project.aapt2Enabled: Boolean
        get() = ProjectOptions(this).get(BooleanOption.ENABLE_AAPT2)

}
