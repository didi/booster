package com.didiglobal.booster.gradle

/**
 *  Represents android transform for feature project
 *
 * @author johnsonlee
 */
class BoosterFeatureTransform : BoosterTransform() {

    override fun getScopes() = SCOPE_FULL_LIBRARY_WITH_FEATURES

}
