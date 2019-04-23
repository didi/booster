package com.didiglobal.booster.gradle

import com.android.build.api.transform.QualifiedContent
import com.android.build.gradle.internal.pipeline.TransformManager

/**
 * Represents android transform for library project
 *
 * @author johnsonlee
 */
class BoosterLibTransform : BoosterTransform() {

    override fun getScopes(): MutableSet<in QualifiedContent.Scope> = TransformManager.PROJECT_ONLY

}
