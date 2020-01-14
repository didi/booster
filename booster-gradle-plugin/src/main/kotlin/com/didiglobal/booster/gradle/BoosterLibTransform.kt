package com.didiglobal.booster.gradle

import com.android.build.api.transform.QualifiedContent
import com.android.build.gradle.internal.pipeline.TransformManager
import org.gradle.api.Project

/**
 * Represents android transform for library project
 *
 * @author johnsonlee
 */
class BoosterLibTransform(project: Project) : BoosterTransform(project) {

    override fun getScopes(): MutableSet<in QualifiedContent.Scope> = when {
        transformers.isEmpty() -> super.getScopes()
        else -> TransformManager.PROJECT_ONLY
    }

    override fun getReferencedScopes(): MutableSet<in QualifiedContent.Scope> = when {
        transformers.isEmpty() -> TransformManager.PROJECT_ONLY
        else -> super.getReferencedScopes()
    }

}
