package com.didiglobal.booster.gradle

import com.android.build.api.transform.QualifiedContent
import org.gradle.api.Project

/**
 * Represents android transform for application project
 *
 * @author johnsonlee
 */
class BoosterAppTransform(project: Project) : BoosterTransform(project) {

    override fun getScopes(): MutableSet<in QualifiedContent.Scope> = when {
        transformers.isEmpty() -> super.getScopes()
        else -> SCOPE_FULL_WITH_FEATURES
    }

    override fun getReferencedScopes(): MutableSet<in QualifiedContent.Scope> = when {
        transformers.isEmpty() -> SCOPE_FULL_WITH_FEATURES
        else -> super.getReferencedScopes()
    }

}
