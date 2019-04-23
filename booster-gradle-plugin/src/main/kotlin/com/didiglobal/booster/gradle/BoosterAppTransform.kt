package com.didiglobal.booster.gradle

import com.android.build.api.transform.QualifiedContent
import com.android.build.gradle.internal.pipeline.TransformManager

/**
 * Represents android transform for application project
 *
 * @author johnsonlee
 */
class BoosterAppTransform : BoosterTransform() {

    override fun getScopes(): MutableSet<in QualifiedContent.Scope> = TransformManager.SCOPE_FULL_PROJECT

}
