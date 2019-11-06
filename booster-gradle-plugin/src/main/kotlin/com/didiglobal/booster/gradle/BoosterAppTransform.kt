package com.didiglobal.booster.gradle

import com.android.build.api.transform.QualifiedContent

/**
 * Represents android transform for application project
 *
 * @author johnsonlee
 */
class BoosterAppTransform : BoosterTransform() {

    override fun getScopes(): MutableSet<in QualifiedContent.Scope> = SCOPE_FULL_WITH_FEATURES

}
