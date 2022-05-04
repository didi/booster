package com.didiglobal.booster.android.gradle.v3_6

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
import com.android.build.gradle.internal.scope.VariantScope
import com.android.build.gradle.internal.variant.BaseVariantData
import com.android.builder.core.VariantType
import com.android.builder.model.ApiVersion
import com.android.sdklib.AndroidVersion
import com.android.sdklib.BuildToolInfo
import com.didiglobal.booster.gradle.AGPInterface
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.artifacts.ArtifactCollection
import org.gradle.api.file.FileCollection
import org.gradle.api.file.FileSystemLocation
import org.gradle.api.tasks.TaskProvider
import java.util.TreeMap

@Suppress("UnstableApiUsage")
internal val ARTIFACT_TYPES = arrayOf(
        AnchorOutputType::class,
        BuildArtifactType::class,
        SourceArtifactType::class,
        InternalArtifactType::class
).map {
    it.sealedSubclasses
}.flatten().map {
    it.objectInstance as ArtifactType<out FileSystemLocation>
}.associateBy {
    it.javaClass.simpleName
}

object V36 : AGPInterface {

    @Suppress("UnstableApiUsage")
    private fun <T : FileSystemLocation> BaseVariant.getFinalArtifactFiles(type: ArtifactType<T>): FileCollection {
        return try {
            project.objects.fileCollection().from(variantScope.artifacts.getFinalProduct(type))
        } catch (e: Throwable) {
            project.objects.fileCollection().builtBy(variantScope.artifacts.getFinalProduct(type))
        }
    }

    override val scopeFullWithFeatures: MutableSet<in QualifiedContent.Scope>
        get() = TransformManager.SCOPE_FULL_WITH_FEATURES

    override val scopeFullLibraryWithFeatures: MutableSet<in QualifiedContent.Scope>
        get() = (TransformManager.SCOPE_FEATURES + QualifiedContent.Scope.PROJECT).toMutableSet()

    override val BaseVariant.project: Project
        get() = globalScope.project

    override val BaseVariant.javaCompilerTaskProvider: TaskProvider<out Task>
        get() = javaCompileProvider

    override val BaseVariant.preBuildTaskProvider: TaskProvider<out Task>
        get() = preBuildProvider

    override val BaseVariant.assembleTaskProvider: TaskProvider<out Task>
        get() = assembleProvider

    override val BaseVariant.mergeAssetsTaskProvider: TaskProvider<out Task>
        get() = mergeAssetsProvider

    override val BaseVariant.mergeResourcesTaskProvider: TaskProvider<out Task>
        get() = try {
            project.tasks.named(getTaskName("merge", "Resources"))
        } catch (e: Throwable) {
            mergeResourcesProvider
        }

    override val BaseVariant.mergeNativeLibsTaskProvider: TaskProvider<out Task>
        get() = project.tasks.named(getTaskName("merge", "NativeLibs"))

    override val BaseVariant.processJavaResourcesTaskProvider: TaskProvider<out Task>
        get() = processJavaResourcesProvider

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

    override val BaseVariant.rawAndroidResources: FileCollection
        get() = variantData.allRawAndroidResources

    override fun BaseVariant.getArtifactCollection(
            configType: AndroidArtifacts.ConsumedConfigType,
            scope: AndroidArtifacts.ArtifactScope,
            artifactType: AndroidArtifacts.ArtifactType
    ): ArtifactCollection {
        return variantScope.getArtifactCollection(configType, scope, artifactType)
    }

    override fun BaseVariant.getArtifactFileCollection(
            configType: AndroidArtifacts.ConsumedConfigType,
            scope: AndroidArtifacts.ArtifactScope,
            artifactType: AndroidArtifacts.ArtifactType
    ): FileCollection {
        return variantScope.getArtifactFileCollection(configType, scope, artifactType)
    }

    override val BaseVariant.allArtifacts: Map<String,FileCollection>
        get() = ARTIFACT_TYPES.entries.associateTo(TreeMap()) { (name, type) ->
            val artifacts: FileCollection by lazy {
                getFinalArtifactFiles(type)
            }
            name to artifacts
        }

    override val BaseVariant.minSdkVersion: AndroidVersion
        get() = variantData.variantConfiguration.minSdkVersion

    override val BaseVariant.targetSdkVersion: ApiVersion
        get() = variantData.variantConfiguration.targetSdkVersion

    override val BaseVariant.variantType: VariantType
        get() = variantData.type

    override val BaseVariant.aar: FileCollection
        get() = getFinalArtifactFiles(InternalArtifactType.AAR)

    override val BaseVariant.apk: FileCollection
        get() = getFinalArtifactFiles(InternalArtifactType.APK)

    override val BaseVariant.mergedManifests: FileCollection
        get() = when (this) {
            is ApplicationVariant -> getFinalArtifactFiles(InternalArtifactType.MERGED_MANIFESTS)
            is LibraryVariant -> getFinalArtifactFiles(InternalArtifactType.LIBRARY_MANIFEST)
            else -> TODO("Unsupported variant type: $variantType")
        }

    override val BaseVariant.mergedRes: FileCollection
        get() = getFinalArtifactFiles(InternalArtifactType.MERGED_RES)

    override val BaseVariant.mergedNativeLibs: FileCollection
        get() = getFinalArtifactFiles(InternalArtifactType.MERGED_NATIVE_LIBS)

    @Suppress("UnstableApiUsage")
    override val BaseVariant.mergedAssets: FileCollection
        get() = getFinalArtifactFiles(when (this) {
            is ApplicationVariant -> InternalArtifactType.MERGED_ASSETS
            is LibraryVariant -> InternalArtifactType.LIBRARY_ASSETS
            else -> TODO("Unsupported variant type: $variantType")
        })

    override val BaseVariant.processedRes: FileCollection
        get() = getFinalArtifactFiles(InternalArtifactType.PROCESSED_RES)

    override val BaseVariant.symbolList: FileCollection
        get() = getFinalArtifactFiles(when (this) {
            is ApplicationVariant -> InternalArtifactType.RUNTIME_SYMBOL_LIST
            is LibraryVariant -> InternalArtifactType.COMPILE_SYMBOL_LIST
            else -> TODO("Unsupported variant type : $variantType")
        })

    override val BaseVariant.symbolListWithPackageName: FileCollection
        get() = getFinalArtifactFiles(InternalArtifactType.SYMBOL_LIST_WITH_PACKAGE_NAME)

    override val BaseVariant.dataBindingDependencyArtifacts: FileCollection
        get() = getFinalArtifactFiles(InternalArtifactType.DATA_BINDING_DEPENDENCY_ARTIFACTS)

    override val BaseVariant.allClasses: FileCollection
        get() = getFinalArtifactFiles(AnchorOutputType.ALL_CLASSES)

    override val BaseVariant.buildTools: BuildToolInfo
        get() = globalScope.sdkComponents.buildToolInfoProvider.get()

    override val BaseVariant.isPrecompileDependenciesResourcesEnabled: Boolean
        get() = variantScope.isPrecompileDependenciesResourcesEnabled

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
