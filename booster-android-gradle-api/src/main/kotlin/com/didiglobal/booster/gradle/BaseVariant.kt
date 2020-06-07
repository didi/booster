package com.didiglobal.booster.gradle

import com.android.build.gradle.BaseExtension
import com.android.build.gradle.api.BaseVariant
import com.android.build.gradle.internal.scope.VariantScope
import com.android.build.gradle.internal.variant.BaseVariantData
import com.android.build.gradle.tasks.ProcessAndroidResources
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.artifacts.result.ResolvedArtifactResult
import java.io.File

/**
 * The `android` extension associates with this variant
 */
val BaseVariant.extension: BaseExtension
    get() = scope.extension

/**
 * The location of `$ANDROID_HOME`/platforms/android-`${compileSdkVersion}`
 */
val BaseVariant.platform: File
    get() = extension.run {
        sdkDirectory.resolve("platforms").resolve(compileSdkVersion)
    }

/**
 * The variant dependencies
 */
val BaseVariant.dependencies: Collection<ResolvedArtifactResult>
    get() = ResolvedArtifactResults(this)

/**
 * The variant scope
 */
val BaseVariant.scope: VariantScope
    get() = variantData.scope

/**
 * The project which this variant belongs
 */
val BaseVariant.project: Project
    get() = scope.globalScope.project

/**
 * The variant data
 */
val BaseVariant.variantData: BaseVariantData
    get() = javaClass.getDeclaredMethod("getVariantData").invoke(this) as BaseVariantData

/**
 * The `compileJava` task associates with this variant
 */
@Suppress("DEPRECATION")
val BaseVariant.javaCompilerTask: Task
    get() = if (GTE_V3_3) {
        this.javaCompileProvider.get()
    } else {
        this.javaCompiler
    }

/**
 * The `preBuild` task associates with this variant
 */
@Suppress("DEPRECATION")
val BaseVariant.preBuildTask: Task
    get() = if (GTE_V3_3) {
        this.preBuildProvider.get()
    } else {
        this.preBuild
    }

/**
 * The `assemble` task associates with this variant
 */
@Suppress("DEPRECATION")
val BaseVariant.assembleTask: Task
    get() = if (GTE_V3_3) {
        this.assembleProvider.get()
    } else {
        this.assemble
    }

/**
 * The `mergeAssets` task associates with this variant
 */
@Suppress("DEPRECATION")
val BaseVariant.mergeAssetsTask: Task
    get() = if (GTE_V3_3) {
        this.mergeAssetsProvider.get()
    } else {
        this.mergeAssets
    }

/**
 * The `mergeResources` task associates with this variant
 */
@Suppress("DEPRECATION")
val BaseVariant.mergeResourcesTask: Task
    get() = if (GTE_V3_3) {
        this.mergeResourcesProvider.get()
    } else {
        this.mergeResources
    }

/**
 * The `processRes` task associates with this variant
 */
val BaseVariant.processResTask: ProcessAndroidResources
    get() = when {
        GTE_V3_6 -> VariantScopeV36.getProcessResourcesTask(scope)
        GTE_V3_5 -> VariantScopeV35.getProcessResourcesTask(scope)
        GTE_V3_3 -> VariantScopeV33.getProcessResourcesTask(scope)
        GTE_V3_2 -> VariantScopeV32.getProcessResourcesTask(scope)
        else -> VariantScopeV30.getProcessResourcesTask(scope)
    }
