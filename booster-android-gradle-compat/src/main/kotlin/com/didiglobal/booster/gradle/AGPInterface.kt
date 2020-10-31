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
import com.android.build.gradle.internal.scope.GlobalScope
import com.android.build.gradle.internal.scope.VariantScope
import com.android.build.gradle.internal.variant.BaseVariantData
import com.android.builder.core.VariantType
import com.android.builder.model.ApiVersion
import com.android.sdklib.AndroidVersion
import com.android.sdklib.BuildToolInfo
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.artifacts.ArtifactCollection
import java.io.File

interface AGPInterface {

    val scopeFullWithFeatures: MutableSet<in QualifiedContent.Scope>
        get() = TransformManager.SCOPE_FULL_PROJECT

    val scopeFullLibraryWithFeatures: MutableSet<in QualifiedContent.Scope>
        get() = TransformManager.PROJECT_ONLY

    val BaseVariant.project: Project

    val BaseVariant.javaCompilerTask: Task

    val BaseVariant.preBuildTask: Task

    val BaseVariant.assembleTask: Task

    val BaseVariant.mergeAssetsTask: Task

    val BaseVariant.mergeResourcesTask: Task

    fun BaseVariant.getTaskName(prefix: String): String

    fun BaseVariant.getTaskName(prefix: String, suffix: String): String

    val BaseVariant.variantData: BaseVariantData

    val BaseVariant.variantScope: VariantScope

    val BaseVariant.globalScope: GlobalScope

    val BaseVariant.originalApplicationId: String

    val BaseVariant.hasDynamicFeature: Boolean

    val BaseVariant.rawAndroidResources: Collection<File>

    fun BaseVariant.getArtifactCollection(
            configType: AndroidArtifacts.ConsumedConfigType,
            scope: AndroidArtifacts.ArtifactScope,
            artifactType: AndroidArtifacts.ArtifactType
    ): ArtifactCollection

    val BaseVariant.allArtifacts: Map<String, Collection<File>>

    val BaseVariant.minSdkVersion: AndroidVersion

    val BaseVariant.targetSdkVersion: ApiVersion

    val BaseVariant.variantType: VariantType

    val BaseVariant.aar: Collection<File>

    val BaseVariant.apk: Collection<File>

    val BaseVariant.mergedManifests: Collection<File>

    val BaseVariant.mergedRes: Collection<File>

    val BaseVariant.mergedAssets: Collection<File>

    val BaseVariant.processedRes: Collection<File>

    val BaseVariant.symbolList: Collection<File>

    val BaseVariant.symbolListWithPackageName: Collection<File>

    val BaseVariant.dataBindingDependencyArtifacts: Collection<File>

    val BaseVariant.allClasses: Collection<File>

    val BaseVariant.buildTools: BuildToolInfo

    val Context.task: TransformTask

    val Project.aapt2Enabled: Boolean

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
