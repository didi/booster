package com.didiglobal.booster.transform

/**
 * Represents the transform lifecycle listener
 *
 * @author johnsonlee
 */
interface TransformListener {

    fun onPreTransform(context: TransformContext) {}

    fun onPostTransform(context: TransformContext) {}

}