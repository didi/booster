package com.didiglobal.booster.android.gradle.v4_1

import com.android.build.api.artifact.Artifact
import com.android.build.api.artifact.ArtifactType
import com.android.build.api.artifact.MultipleArtifactType
import com.android.build.api.artifact.impl.ArtifactsImpl
import com.android.build.api.component.impl.ComponentPropertiesImpl
import com.android.build.api.transform.Context
import com.android.build.api.transform.QualifiedContent
import com.android.build.gradle.api.ApplicationVariant
import com.android.build.gradle.api.BaseVariant
import com.android.build.gradle.api.LibraryVariant
import com.android.build.gradle.internal.api.BaseVariantImpl
import com.android.build.gradle.internal.pipeline.TransformManager
import com.android.build.gradle.internal.pipeline.TransformTask
import com.android.build.gradle.internal.publishing.AndroidArtifacts
import com.android.build.gradle.internal.publishing.AndroidArtifacts.ArtifactScope
import com.android.build.gradle.internal.scope.AnchorOutputType
import com.android.build.gradle.internal.scope.GlobalScope
import com.android.build.gradle.internal.scope.InternalArtifactType
import com.android.build.gradle.internal.scope.InternalMultipleArtifactType
import com.android.build.gradle.internal.scope.VariantScope
import com.android.build.gradle.internal.variant.BaseVariantData
import com.android.builder.core.DefaultApiVersion
import com.android.builder.core.VariantType
import com.android.builder.model.ApiVersion
import com.android.sdklib.BuildToolInfo
import com.didiglobal.booster.gradle.AGPInterface
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.artifacts.ArtifactCollection
import org.gradle.api.artifacts.component.ComponentIdentifier
import org.gradle.api.artifacts.result.ResolvedArtifactResult
import org.gradle.api.artifacts.result.ResolvedDependencyResult
import org.gradle.api.file.FileCollection
import org.gradle.api.file.FileSystemLocation
import org.gradle.api.tasks.TaskProvider
import java.io.File
import java.util.TreeMap

@Suppress("UnstableApiUsage")
private val SINGLE_ARTIFACT_TYPES = arrayOf(
        ArtifactType::class,
        AnchorOutputType::class,
        InternalArtifactType::class
).map {
    it.sealedSubclasses
}.flatten().map {
    it.objectInstance as Artifact.SingleArtifact<out FileSystemLocation>
}.associateBy {
    it.javaClass.simpleName
}

@Suppress("UnstableApiUsage")
private val MULTIPLE_ARTIFACT_TYPES = arrayOf(
        MultipleArtifactType::class,
        InternalMultipleArtifactType::class
).map {
    it.sealedSubclasses
}.flatten().map {
    it.objectInstance as Artifact.MultipleArtifact<out FileSystemLocation>
}.associateBy {
    it.javaClass.simpleName
}

internal object V41 : AGPInterface {

    private val BaseVariant.componentProperties: ComponentPropertiesImpl
        get() = BaseVariantImpl::class.java.getDeclaredField("componentProperties").apply {
            isAccessible = true
        }.get(this) as ComponentPropertiesImpl

    @Suppress("UnstableApiUsage")
    private fun <T : FileSystemLocation> BaseVariant.getFinalArtifactFiles(type: Artifact.SingleArtifact<T>): FileCollection {
        return try {
            project.objects.fileCollection().from(artifacts.get(type))
        } catch (e: Throwable) {
            project.logger.warn(e.message, e)
            project.objects.fileCollection().builtBy(artifacts.get(type))
        }
    }

    @Suppress("UnstableApiUsage")
    private fun <T : FileSystemLocation> BaseVariant.getFinalArtifactFiles(type: Artifact.MultipleArtifact<T>): FileCollection {
        return try {
            project.objects.fileCollection().from(artifacts.getAll(type))
        } catch (e: Throwable) {
            project.logger.warn(e.message, e)
            project.objects.fileCollection().builtBy(artifacts.getAll(type))
        }
    }

    @Suppress("UnstableApiUsage")
    private val BaseVariant.artifacts: ArtifactsImpl
        get() = componentProperties.artifacts

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
        return componentProperties.computeTaskName(prefix)
    }

    override fun BaseVariant.getTaskName(prefix: String, suffix: String): String {
        return componentProperties.computeTaskName(prefix, suffix)
    }

    override val BaseVariant.variantData: BaseVariantData
        get() = javaClass.getDeclaredMethod("getVariantData").apply {
            isAccessible = true
        }.invoke(this) as BaseVariantData

    override val BaseVariant.variantScope: VariantScope
        get() = componentProperties.variantScope

    private val BaseVariant.globalScope: GlobalScope
        get() = componentProperties.globalScope

    override val BaseVariant.originalApplicationId: String
        get() = componentProperties.variantDslInfo.packageName.get()

    override val BaseVariant.hasDynamicFeature: Boolean
        get() = globalScope.hasDynamicFeatures()

    override val BaseVariant.rawAndroidResources: FileCollection
        get() = componentProperties.variantData.allRawAndroidResources

    override fun BaseVariant.getArtifactCollection(
            configType: AndroidArtifacts.ConsumedConfigType,
            scope: ArtifactScope,
            artifactType: AndroidArtifacts.ArtifactType
    ): ArtifactCollection {
        return componentProperties.variantDependencies.getArtifactCollection(configType, scope, artifactType)
    }

    override fun BaseVariant.getArtifactFileCollection(
            configType: AndroidArtifacts.ConsumedConfigType,
            scope: ArtifactScope,
            artifactType: AndroidArtifacts.ArtifactType
    ): FileCollection {
        return componentProperties.variantDependencies.getArtifactFileCollection(configType, scope, artifactType)
    }

    override val BaseVariant.allArtifacts: Map<String, FileCollection>
        get() = TreeMap<String, FileCollection>().also { all ->
            SINGLE_ARTIFACT_TYPES.entries.associateTo(all) { (name, type) ->
                val artifacts: FileCollection by lazy {
                    getFinalArtifactFiles(type)
                }
                name to artifacts
            }
            MULTIPLE_ARTIFACT_TYPES.entries.associateTo(all) { (name, type) ->
                val artifacts: FileCollection by lazy {
                    getFinalArtifactFiles(type)
                }
                name to artifacts
            }
        }

    override val BaseVariant.minSdkVersion: ApiVersion
        get() = DefaultApiVersion(componentProperties.minSdkVersion.apiLevel)

    override val BaseVariant.targetSdkVersion: ApiVersion
        get() = componentProperties.targetSdkVersion

    private val BaseVariant.variantType: VariantType
        get() = componentProperties.variantType

    override val BaseVariant.isApplication: Boolean
        get() = variantType.isApk

    override val BaseVariant.isLibrary: Boolean
        get() = variantType.isAar

    override val BaseVariant.isDynamicFeature: Boolean
        get() = variantType.isDynamicFeature

    override val BaseVariant.aar: FileCollection
        get() = getFinalArtifactFiles(InternalArtifactType.AAR)

    override val BaseVariant.apk: FileCollection
        get() = getFinalArtifactFiles(ArtifactType.APK)

    override val BaseVariant.mergedManifests: FileCollection
        get() = getFinalArtifactFiles(ArtifactType.MERGED_MANIFEST)

    override val BaseVariant.mergedRes: FileCollection
        get() = getFinalArtifactFiles(InternalArtifactType.MERGED_RES)

    override val BaseVariant.mergedNativeLibs: FileCollection
        get() = getFinalArtifactFiles(InternalArtifactType.MERGED_NATIVE_LIBS)

    override val BaseVariant.mergedAssets: FileCollection
        get() = when (this) {
            is ApplicationVariant -> getFinalArtifactFiles(InternalArtifactType.MERGED_ASSETS)
            is LibraryVariant -> getFinalArtifactFiles(InternalArtifactType.LIBRARY_ASSETS)
            else -> TODO("Unsupported variant type: $variantType")
        }

    override val BaseVariant.processedRes: FileCollection
        get() = getFinalArtifactFiles(InternalArtifactType.PROCESSED_RES)

    override val BaseVariant.symbolList: FileCollection
        get() = when (this) {
            is ApplicationVariant -> getFinalArtifactFiles(InternalArtifactType.RUNTIME_SYMBOL_LIST)
            is LibraryVariant -> getFinalArtifactFiles(InternalArtifactType.COMPILE_SYMBOL_LIST)
            else -> TODO("Unsupported variant type : $variantType")
        }

    override val BaseVariant.symbolListWithPackageName: FileCollection
        get() = getFinalArtifactFiles(InternalArtifactType.SYMBOL_LIST_WITH_PACKAGE_NAME)

    override val BaseVariant.dataBindingDependencyArtifacts: FileCollection
        get() = getFinalArtifactFiles(InternalArtifactType.DATA_BINDING_DEPENDENCY_ARTIFACTS)

    override val BaseVariant.allClasses: FileCollection
        get() = when (this) {
            is ApplicationVariant -> getFinalArtifactFiles(InternalArtifactType.JAVAC) + project.files("build${File.separator}tmp${File.separator}kotlin-classes${File.separator}${dirName}")
            is LibraryVariant -> getFinalArtifactFiles(InternalArtifactType.AAR_MAIN_JAR)
            else -> project.files()
        }

    override val BaseVariant.buildTools: BuildToolInfo
        get() = globalScope.sdkComponents.get().buildToolInfoProvider.get()

    override val BaseVariant.isPrecompileDependenciesResourcesEnabled: Boolean
        get() = variantScope.isPrecompileDependenciesResourcesEnabled

    override fun BaseVariant.getDependencies(transitive: Boolean, filter: (ComponentIdentifier) -> Boolean): Collection<ResolvedArtifactResult> {
        val all = getArtifactCollection(
                AndroidArtifacts.ConsumedConfigType.RUNTIME_CLASSPATH,
                AndroidArtifacts.ArtifactScope.ALL,
                AndroidArtifacts.ArtifactType.CLASSES_JAR
        ).filter { result ->
            filter(result.id.componentIdentifier)
        }.associateBy {
            it.id.componentIdentifier.displayName
        }
        val result = if (!transitive) {
            runtimeConfiguration.incoming.resolutionResult.root.dependencies.filterIsInstance<ResolvedDependencyResult>().mapNotNull {
                it.selected.id.displayName.takeIf { id -> id in all.keys }
            }.associateWith {
                all[it]!!
            }
        } else {
            all
        }
        return result.values.toSet()
    }

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
