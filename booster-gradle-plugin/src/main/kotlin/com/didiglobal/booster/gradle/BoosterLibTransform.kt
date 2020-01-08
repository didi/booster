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

    override fun getScopes(): MutableSet<in QualifiedContent.Scope> = TransformManager.PROJECT_ONLY

}
