package com.didiglobal.booster.android.gradle.v9_0

import com.android.build.api.artifact.Artifact
import com.android.build.api.artifact.MultipleArtifact
import com.android.build.api.artifact.SingleArtifact
import com.android.build.api.artifact.impl.ArtifactsImpl
import com.android.build.api.component.analytics.AnalyticsEnabledVariant
import com.android.build.api.component.impl.ComponentImpl
import com.android.build.api.dsl.CommonExtension
import com.android.build.api.variant.AndroidVersion
import com.android.build.api.variant.ApplicationVariant
import com.android.build.api.variant.GeneratesApk
import com.android.build.api.variant.LibraryVariant
import com.android.build.api.variant.Variant
import com.android.build.api.variant.impl.VariantImpl
import com.android.build.gradle.internal.api.artifact.SourceArtifactType
import com.android.build.gradle.internal.publishing.AndroidArtifacts
import com.android.build.gradle.internal.scope.BuildArtifactType
import com.android.build.gradle.internal.scope.InternalArtifactType
import com.android.build.gradle.internal.scope.InternalMultipleArtifactType
import com.android.build.gradle.internal.tasks.factory.GlobalTaskCreationConfigImpl
import com.android.build.gradle.internal.variant.BaseVariantData
import com.android.sdklib.BuildToolInfo
import com.didiglobal.booster.gradle.AGPInterface
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.artifacts.ArtifactCollection
import org.gradle.api.artifacts.component.ComponentIdentifier
import org.gradle.api.artifacts.result.ResolvedArtifactResult
import org.gradle.api.artifacts.result.ResolvedDependencyResult
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.FileCollection
import org.gradle.api.file.FileSystemLocation
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.TaskProvider
import java.util.TreeMap
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.jvm.isAccessible

@Suppress("UnstableApiUsage")
private val SINGLE_ARTIFACT_TYPES = arrayOf(
        BuildArtifactType::class,
        InternalArtifactType::class,
        SingleArtifact::class,
        SourceArtifactType::class
).map {
    it.sealedSubclasses
}.flatten().map {
    it.objectInstance as Artifact.Single<out FileSystemLocation>
}.associateBy {
    it.javaClass.simpleName
}

private val MULTIPLE_ARTIFACT_TYPES = arrayOf(
        MultipleArtifact::class,
        InternalMultipleArtifactType::class
).map {
    it.sealedSubclasses
}.flatten().map {
    it.objectInstance as Artifact.Multiple<out FileSystemLocation>
}.associateBy {
    it.javaClass.simpleName
}

/**
 * AGP 9.0 implementation
 *
 * Supports the AGP 9.0 public DSL, Variant API, and built-in Kotlin defaults.
 */
@Suppress("DEPRECATION")
internal object V90 : AGPInterface {

    private val Variant.component: VariantImpl<*>
        get() = when (this) {
            is VariantImpl<*> -> this
            is AnalyticsEnabledVariant -> this.delegate as VariantImpl<*>
            else -> TODO("No implemented!")
        }

    @Suppress("UnstableApiUsage")
    private fun <T : FileSystemLocation> Variant.getFinalArtifactFiles(type: Artifact.Single<T>): FileCollection {
        return project.objects.fileCollection().from(artifactsImpl.get(type))
    }

    @Suppress("UnstableApiUsage")
    private fun <T : FileSystemLocation> Variant.getFinalArtifactFiles(type: Artifact.Multiple<T>): FileCollection {
        return project.objects.fileCollection().from(artifactsImpl.getAll(type))
    }

    @Suppress("UnstableApiUsage")
    private val Variant.artifactsImpl: ArtifactsImpl
        get() = component.artifacts

    override val Variant.project: Project
        get() {
            return this.component.variantDependencies.javaClass.kotlin.declaredMemberProperties.first {
                it.name == "project"
            }.apply {
                isAccessible = true
            }.get(this.component.variantDependencies) as Project
        }

    override val Variant.javaCompilerTaskProvider: TaskProvider<out Task>
        get() = component.taskContainer.javacTask

    override val Variant.preBuildTaskProvider: TaskProvider<out Task>
        get() = component.taskContainer.preBuildTask

    override val Variant.assembleTaskProvider: TaskProvider<out Task>
        get() = component.taskContainer.assembleTask

    override val Variant.mergeAssetsTaskProvider: TaskProvider<out Task>
        get() = component.taskContainer.mergeAssetsTask

    override val Variant.mergeResourcesTaskProvider: TaskProvider<out Task>
        get() = project.tasks.named(getTaskName("merge", "Resources"))

    override val Variant.mergeNativeLibsTaskProvider: TaskProvider<out Task>
        get() = project.tasks.named(getTaskName("merge", "NativeLibs"))

    override val Variant.processJavaResourcesTaskProvider: TaskProvider<out Task>
        get() = component.taskContainer.processJavaResourcesTask

    override fun Variant.getTaskName(prefix: String): String {
        return prefix + name.replaceFirstChar(Char::uppercaseChar)
    }

    override fun Variant.getTaskName(prefix: String, suffix: String): String {
        return component.computeTaskName(prefix, suffix)
    }

    override val Variant.variantData: BaseVariantData
        get() = ComponentImpl::class.java.getDeclaredField("variantData").apply {
            isAccessible = true
        }.get(component) as BaseVariantData

    @Suppress("DEPRECATION")
    private val Variant.globalScope: GlobalTaskCreationConfigImpl
        get() = component.global as GlobalTaskCreationConfigImpl

    override val Variant.originalApplicationId: String
        get() = requireNotNull(project.extensions.getByType(CommonExtension::class.java).namespace)

    override val Variant.hasDynamicFeature: Boolean
        get() = component.global.hasDynamicFeatures

    override val Variant.rawAndroidResources: FileCollection
        get() {
            val allRes: ConfigurableFileCollection = component.services.fileCollection()

            allRes.from(
                    component.variantDependencies.getArtifactCollection(
                            AndroidArtifacts.ConsumedConfigType.RUNTIME_CLASSPATH,
                            AndroidArtifacts.ArtifactScope.ALL,
                            AndroidArtifacts.ArtifactType.ANDROID_RES
                    ).artifactFiles
            )

            allRes.from(component.sources.res?.all)

            return allRes
        }
    override val Variant.sourceSetMap: FileCollection
        get() = getFinalArtifactFiles(InternalArtifactType.ANDROID_RES_SOURCE_SET_PATH_MAP)

    override val Variant.localAndroidResources: FileCollection
        get() = component.services.fileCollection().from(component.sources.res?.static)

    override fun <T : FileSystemLocation> Variant.getSingleArtifact(type: Artifact.Single<T>): Provider<T> {
        return artifactsImpl.get(type)
    }

    override fun Variant.getArtifactCollection(
            configType: AndroidArtifacts.ConsumedConfigType,
            scope: AndroidArtifacts.ArtifactScope,
            artifactType: AndroidArtifacts.ArtifactType
    ): ArtifactCollection {
        return component.variantDependencies.getArtifactCollection(configType, scope, artifactType)
    }

    override fun Variant.getArtifactFileCollection(
            configType: AndroidArtifacts.ConsumedConfigType,
            scope: AndroidArtifacts.ArtifactScope,
            artifactType: AndroidArtifacts.ArtifactType
    ): FileCollection {
        return component.variantDependencies.getArtifactFileCollection(configType, scope, artifactType)
    }

    override val Variant.allArtifacts: Map<String, FileCollection>
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

    override val Variant.targetVersion: AndroidVersion
        get() = when (this) {
            is GeneratesApk -> targetSdk
            else -> javaClass.getMethod("getTargetSdkVersion").invoke(this) as AndroidVersion
        }

    override val Variant.isApplication: Boolean
        get() = component.componentType.isApk

    override val Variant.isLibrary: Boolean
        get() = component.componentType.isAar

    override val Variant.isDynamicFeature: Boolean
        get() = component.componentType.isDynamicFeature

    override val Variant.aar: FileCollection
        get() = getFinalArtifactFiles(SingleArtifact.AAR)

    override val Variant.apk: FileCollection
        get() = getFinalArtifactFiles(SingleArtifact.APK)

    override val Variant.mergedManifests: FileCollection
        get() = getFinalArtifactFiles(SingleArtifact.MERGED_MANIFEST)

    override val Variant.mergedRes: FileCollection
        get() = getFinalArtifactFiles(InternalArtifactType.MERGED_RES)

    override val Variant.mergedNativeLibs: FileCollection
        get() = getFinalArtifactFiles(SingleArtifact.MERGED_NATIVE_LIBS)

    override val Variant.mergedAssets: FileCollection
        get() = getFinalArtifactFiles(SingleArtifact.ASSETS)

    override val Variant.processedRes: FileCollection
        get() = getFinalArtifactFiles(InternalArtifactType.PACKAGED_RES)

    override val Variant.symbolList: FileCollection
        get() = when (this) {
            is ApplicationVariant -> getFinalArtifactFiles(SingleArtifact.RUNTIME_SYMBOL_LIST)
            is LibraryVariant -> getFinalArtifactFiles(InternalArtifactType.COMPILE_SYMBOL_LIST)
            else -> TODO("Unsupported variant type: $name@${javaClass.name}")
        }

    override val Variant.symbolListWithPackageName: FileCollection
        get() = getFinalArtifactFiles(InternalArtifactType.SYMBOL_LIST_WITH_PACKAGE_NAME)

    override val Variant.dataBindingDependencyArtifacts: FileCollection
        get() = if (component.buildFeatures.dataBinding) {
            getFinalArtifactFiles(InternalArtifactType.DATA_BINDING_DEPENDENCY_ARTIFACTS)
        } else {
            project.files()
        }

    override val Variant.allClasses: FileCollection
        get() = when {
            isApplication -> getFinalArtifactFiles(InternalArtifactType.JAVAC) +
                    getFinalArtifactFiles(InternalArtifactType.BUILT_IN_KOTLINC)
            isLibrary -> getFinalArtifactFiles(InternalArtifactType.AAR_MAIN_JAR)
            else -> project.files()
        }

    override val Variant.buildTools: BuildToolInfo
        get() = globalScope.versionedSdkLoader.get().buildToolInfoProvider.get()

    override val Variant.isPrecompileDependenciesResourcesEnabled: Boolean
        get() = component.androidResourcesCreationConfig?.isPrecompileDependenciesResourcesEnabled == true

    override val Variant.isDebuggable: Boolean
        get() = component.debuggable

    override fun Variant.getDependencies(
            transitive: Boolean,
            filter: (ComponentIdentifier) -> Boolean
    ): Collection<ResolvedArtifactResult> {
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
            runtimeConfiguration.incoming.resolutionResult.root.dependencies.filterIsInstance<ResolvedDependencyResult>()
                    .mapNotNull {
                        it.selected.id.displayName.takeIf { id -> id in all.keys }
                    }.associateWith {
                        all[it]!!
                    }
        } else {
            all
        }
        return result.values.toSet()
    }

}
