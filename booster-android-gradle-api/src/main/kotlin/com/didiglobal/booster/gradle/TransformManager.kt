package com.didiglobal.booster.gradle

import com.android.build.api.transform.QualifiedContent
import com.android.build.gradle.internal.pipeline.TransformManager

val SCOPE_PROJECT: MutableSet<in QualifiedContent.Scope> = TransformManager.PROJECT_ONLY

val SCOPE_FULL_PROJECT: MutableSet<in QualifiedContent.Scope> = TransformManager.SCOPE_FULL_PROJECT

val SCOPE_FULL_WITH_FEATURES: MutableSet<in QualifiedContent.Scope> = AGP.run {
    scopeFullWithFeatures
}

val SCOPE_FULL_LIBRARY_WITH_FEATURES: MutableSet<in QualifiedContent.Scope> = AGP.run {
    scopeFullLibraryWithFeatures
}
