package com.didiglobal.booster.android.gradle.v8_0

import com.android.build.api.artifact.Artifact
import com.android.build.api.artifact.MultipleArtifact
import com.android.build.api.artifact.SingleArtifact
import com.android.build.api.artifact.impl.ArtifactsImpl
import com.android.build.api.component.analytics.AnalyticsEnabledVariant
import com.android.build.api.variant.AndroidVersion
import com.android.build.api.variant.ApplicationVariant
import com.android.build.api.variant.LibraryVariant
import com.android.build.api.variant.Variant
import com.android.build.api.variant.impl.TaskProviderBasedDirectoryEntryImpl
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
import java.io.File
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

@Suppress("DEPRECATION")
internal object V80 : AGPInterface {

    private val Variant.component: VariantImpl<*>
        get() = when (this) {
            is VariantImpl<*> -> this
            is AnalyticsEnabledVariant -> this.delegate as VariantImpl<*>
            else -> TODO("No implemented!")
        }

    @Suppress("UnstableApiUsage")
    private fun <T : FileSystemLocation> Variant.getFinalArtifactFiles(type: Artifact.Single<T>): FileCollection {
        return try {
            project.objects.fileCollection().from(artifactsImpl.get(type))
        } catch (e: Throwable) {
            project.objects.fileCollection().builtBy(artifactsImpl.get(type))
        }
    }

    @Suppress("UnstableApiUsage")
    private fun <T : FileSystemLocation> Variant.getFinalArtifactFiles(type: Artifact.Multiple<T>): FileCollection {
        return try {
            project.objects.fileCollection().from(artifactsImpl.getAll(type))
        } catch (e: Throwable) {
            project.objects.fileCollection().builtBy(artifactsImpl.getAll(type))
        }
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
        get() = try {
            project.tasks.named(getTaskName("merge", "Resources"))
        } catch (e: Throwable) {
            component.taskContainer.mergeResourcesTask
        }

    override val Variant.mergeNativeLibsTaskProvider: TaskProvider<out Task>
        get() = project.tasks.named(getTaskName("merge", "NativeLibs"))

    override val Variant.processJavaResourcesTaskProvider: TaskProvider<out Task>
        get() = component.taskContainer.processJavaResourcesTask

    override fun Variant.getTaskName(prefix: String): String {
        return component.computeTaskName(prefix)
    }

    override fun Variant.getTaskName(prefix: String, suffix: String): String {
        return component.computeTaskName(prefix, suffix)
    }

    override val Variant.variantData: BaseVariantData
        get() = component.javaClass.getDeclaredMethod("getVariantData").apply {
            isAccessible = true
        }.invoke(this) as BaseVariantData

    @Suppress("DEPRECATION")
    private val Variant.globalScope: GlobalTaskCreationConfigImpl
        get() = component.global as GlobalTaskCreationConfigImpl

    override val Variant.originalApplicationId: String
        get() = component.namespace.get()

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

            allRes.from(
                    component.services.fileCollection(variantData.extraGeneratedResFolders)
                            .builtBy(listOfNotNull(variantData.extraGeneratedResFolders.builtBy))
            )

            component.taskContainer.generateApkDataTask?.let {
                allRes.from(artifactsImpl.get(InternalArtifactType.MICRO_APK_RES))
            }

            component.sources.res { resSources ->
                allRes.from(
                        resSources.getVariantSources().map { directoryEntries ->
                            directoryEntries.directoryEntries
                                    .map {
                                        if (it is TaskProviderBasedDirectoryEntryImpl) {
                                            it.directoryProvider
                                        } else {
                                            it.asFiles(
                                                    component.services.provider {
                                                        component.services.projectInfo.projectDirectory
                                                    })
                                        }
                                    }
                        }
                )
            }

            return allRes
        }
    override val Variant.sourceSetMap: FileCollection
        get() = getFinalArtifactFiles(InternalArtifactType.SOURCE_SET_PATH_MAP)

    override val Variant.localAndroidResources: FileCollection
        get() = component.services.fileCollection().from(component.sources.res?.getLocalSources()?.values?.map {
            it.map { dirs ->
                if (dirs.isEmpty()) {
                    project.files()
                } else {
                    dirs.map { dir ->
                        dir.asFileTree
                    }.reduce { acc, dir ->
                        acc.plus(dir)
                    }
                }
            }
        })

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
        get() = targetSdkVersion

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
        get() = getFinalArtifactFiles(InternalArtifactType.MERGED_NATIVE_LIBS)

    override val Variant.mergedAssets: FileCollection
        get() = when (this) {
            is ApplicationVariant -> getFinalArtifactFiles(InternalArtifactType.COMPRESSED_ASSETS)
            is LibraryVariant -> getFinalArtifactFiles(InternalArtifactType.LIBRARY_ASSETS)
            else -> TODO("Unsupported variant type: $name@${javaClass.name}")
        }

    override val Variant.processedRes: FileCollection
        get() = getFinalArtifactFiles(InternalArtifactType.PROCESSED_RES)

    override val Variant.symbolList: FileCollection
        get() = when (this) {
            is ApplicationVariant -> getFinalArtifactFiles(InternalArtifactType.RUNTIME_SYMBOL_LIST)
            is LibraryVariant -> getFinalArtifactFiles(InternalArtifactType.COMPILE_SYMBOL_LIST)
            else -> TODO("Unsupported variant type : $name@${javaClass.name}")
        }

    override val Variant.symbolListWithPackageName: FileCollection
        get() = getFinalArtifactFiles(InternalArtifactType.SYMBOL_LIST_WITH_PACKAGE_NAME)

    override val Variant.dataBindingDependencyArtifacts: FileCollection
        get() = getFinalArtifactFiles(InternalArtifactType.DATA_BINDING_DEPENDENCY_ARTIFACTS)

    override val Variant.allClasses: FileCollection
        get() = when {
            isApplication -> getFinalArtifactFiles(InternalArtifactType.JAVAC) + project.files("build${File.separator}tmp${File.separator}kotlin-classes${File.separator}${name}")
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
