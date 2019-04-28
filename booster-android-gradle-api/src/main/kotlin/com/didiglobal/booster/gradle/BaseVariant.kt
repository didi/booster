package com.didiglobal.booster.gradle

import com.android.build.gradle.api.BaseVariant
import com.android.build.gradle.internal.api.InstallableVariantImpl
import com.android.build.gradle.internal.scope.VariantScope
import com.android.build.gradle.internal.variant.BaseVariantData
import org.gradle.api.Task

/**
 * The variant dependencies
 *
 * @author johnsonlee
 */
val BaseVariant.dependencies: ResolvedArtifactResults
    get() = ResolvedArtifactResults(this)

/**
 * The variant scope
 *
 * @author johnsonlee
 */
val BaseVariant.scope: VariantScope
    get() = variantData.scope

/**
 * The variant data
 *
 * @author johnsonlee
 */
val BaseVariant.variantData: BaseVariantData
    get() = if (this is InstallableVariantImpl) this.variantData else javaClass.getDeclaredMethod("getVariantData").invoke(this) as BaseVariantData

val BaseVariant.javaCompilerTask: Task
    get() = if (GTE_V33) {
        this.javaCompileProvider.get()
    } else {
        @Suppress("DEPRECATION")
        this.javaCompiler
    }
