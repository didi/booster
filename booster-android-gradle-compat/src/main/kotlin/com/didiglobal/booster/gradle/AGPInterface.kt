package com.didiglobal.booster.gradle

import com.android.build.api.transform.Context
import com.android.build.api.transform.QualifiedContent
import com.android.build.api.transform.TransformInvocation
import com.android.build.gradle.AppExtension
import com.android.build.gradle.BaseExtension
import com.android.build.gradle.LibraryExtension
import com.android.build.gradle.api.BaseVariant
import com.android.build.gradle.internal.pipeline.TransformManager
import com.android.build.gradle.internal.pipeline.TransformTask
import com.android.build.gradle.internal.publishing.AndroidArtifacts
import com.android.build.gradle.internal.scope.VariantScope
import com.android.build.gradle.internal.variant.BaseVariantData
import com.android.builder.model.ApiVersion
import com.android.builder.model.Version
import com.android.repository.Revision
import com.android.sdklib.BuildToolInfo
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.artifacts.ArtifactCollection
import org.gradle.api.artifacts.component.ComponentIdentifier
import org.gradle.api.artifacts.result.ResolvedArtifactResult
import org.gradle.api.file.FileCollection
import org.gradle.api.tasks.TaskProvider
import java.io.File
import java.util.ServiceLoader

interface AGPInterface {

    val revision: Revision
        get() = REVISION

    val scopeFullWithFeatures: MutableSet<in QualifiedContent.Scope>
        get() = TransformManager.SCOPE_FULL_PROJECT

    val scopeFullLibraryWithFeatures: MutableSet<in QualifiedContent.Scope>
        get() = TransformManager.PROJECT_ONLY

    val BaseVariant.project: Project

    @Deprecated(
            message = "Use javaCompilerTaskProvider instead",
            replaceWith = ReplaceWith(expression = "javaCompilerTaskProvider"),
            level = DeprecationLevel.WARNING
    )
    val BaseVariant.javaCompilerTask: Task
        get() = javaCompilerTaskProvider.get()

    @Deprecated(
            message = "Use preBuildTaskProvider instead",
            replaceWith = ReplaceWith(expression = "preBuildTaskProvider"),
            level = DeprecationLevel.WARNING
    )
    val BaseVariant.preBuildTask: Task
        get() = preBuildTaskProvider.get()

    @Deprecated(
            message = "Use preBuildTaskProvider instead",
            replaceWith = ReplaceWith(expression = "preBuildTaskProvider"),
            level = DeprecationLevel.WARNING
    )
    val BaseVariant.assembleTask: Task
        get() = assembleTaskProvider.get()

    @Deprecated(
            message = "Use mergeAssetsTaskProvider instead",
            replaceWith = ReplaceWith(expression = "mergeAssetsTaskProvider"),
            level = DeprecationLevel.WARNING
    )
    val BaseVariant.mergeAssetsTask: Task
        get() = mergeAssetsTaskProvider.get()

    @Deprecated(
            message = "Use mergeResourcesTaskProvider instead",
            replaceWith = ReplaceWith(expression = "mergeResourcesTaskProvider"),
            level = DeprecationLevel.WARNING
    )
    val BaseVariant.mergeResourcesTask: Task
        get() = mergeResourcesTaskProvider.get()


    @Deprecated(
            message = "Use processJavaResourcesTaskProvider instead",
            replaceWith = ReplaceWith(expression = "processJavaResourcesTaskProvider"),
            level = DeprecationLevel.WARNING
    )
    val BaseVariant.processJavaResourcesTask: Task
        get() = processJavaResourcesTaskProvider.get()

    fun BaseVariant.getTaskName(prefix: String): String

    fun BaseVariant.getTaskName(prefix: String, suffix: String): String

    val BaseVariant.variantData: BaseVariantData

    val BaseVariant.variantScope: VariantScope

    @Deprecated(
            message = "Use BaseVariant.namespace instead",
            replaceWith = ReplaceWith(expression = "variant.namespace"),
    )
    val BaseVariant.originalApplicationId: String

    val BaseVariant.hasDynamicFeature: Boolean

    @Deprecated(message = "Deprecated, don't use it")
    val BaseVariant.rawAndroidResources: FileCollection

    val BaseVariant.javaCompilerTaskProvider: TaskProvider<out Task>

    val BaseVariant.preBuildTaskProvider: TaskProvider<out Task>

    val BaseVariant.assembleTaskProvider: TaskProvider<out Task>

    val BaseVariant.mergeAssetsTaskProvider: TaskProvider<out Task>

    val BaseVariant.mergeResourcesTaskProvider: TaskProvider<out Task>

    val BaseVariant.mergeNativeLibsTaskProvider: TaskProvider<out Task>

    val BaseVariant.processJavaResourcesTaskProvider: TaskProvider<out Task>

    fun BaseVariant.getArtifactCollection(
            configType: AndroidArtifacts.ConsumedConfigType,
            scope: AndroidArtifacts.ArtifactScope,
            artifactType: AndroidArtifacts.ArtifactType
    ): ArtifactCollection

    fun BaseVariant.getArtifactFileCollection(
            configType: AndroidArtifacts.ConsumedConfigType,
            scope: AndroidArtifacts.ArtifactScope,
            artifactType: AndroidArtifacts.ArtifactType
    ): FileCollection

    val BaseVariant.allArtifacts: Map<String, FileCollection>

    val BaseVariant.minSdkVersion: ApiVersion

    val BaseVariant.targetSdkVersion: ApiVersion

    val BaseVariant.isApplication: Boolean

    val BaseVariant.isLibrary: Boolean

    val BaseVariant.isDynamicFeature: Boolean

    val BaseVariant.aar: FileCollection

    val BaseVariant.apk: FileCollection

    val BaseVariant.mergedManifests: FileCollection

    val BaseVariant.mergedRes: FileCollection

    val BaseVariant.mergedAssets: FileCollection

    val BaseVariant.mergedNativeLibs: FileCollection

    val BaseVariant.processedRes: FileCollection

    val BaseVariant.symbolList: FileCollection

    val BaseVariant.symbolListWithPackageName: FileCollection

    val BaseVariant.dataBindingDependencyArtifacts: FileCollection

    val BaseVariant.allClasses: FileCollection

    val BaseVariant.buildTools: BuildToolInfo

    val BaseVariant.isPrecompileDependenciesResourcesEnabled: Boolean

    fun BaseVariant.getDependencies(
            transitive: Boolean = true,
            filter: (ComponentIdentifier) -> Boolean = { true }
    ): Collection<ResolvedArtifactResult>

    val Context.task: TransformTask

    @Deprecated(
            message = "Use isAapt2Enabled instead",
            replaceWith = ReplaceWith(
                    expression = "isAapt2Enabled"
            )
    )
    val Project.aapt2Enabled: Boolean

    @Suppress("DEPRECATION")
    val Project.isAapt2Enabled: Boolean
        get() = aapt2Enabled

    val TransformInvocation.variant: BaseVariant
        get() = project.getAndroid<BaseExtension>().let { android ->
            this.context.variantName.let { variant ->
                when (android) {
                    is AppExtension -> when {
                        variant.endsWith("AndroidTest") -> android.testVariants.single { it.name == variant }
                        variant.endsWith("UnitTest") -> android.unitTestVariants.single { it.name == variant }
                        else -> android.applicationVariants.single { it.name == variant }
                    }
                    is LibraryExtension -> android.libraryVariants.single { it.name == variant }
                    else -> TODO("variant not found")
                }
            }
        }

    val TransformInvocation.project: Project
        get() = context.task.project

    val TransformInvocation.bootClasspath: Collection<File>
        get() = project.getAndroid<BaseExtension>().bootClasspath

    val TransformInvocation.isDataBindingEnabled: Boolean
        get() = project.getAndroid<BaseExtension>().dataBinding.isEnabled

}

inline fun <reified T : BaseExtension> Project.getAndroid(): T = extensions.getByName("android") as T

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
