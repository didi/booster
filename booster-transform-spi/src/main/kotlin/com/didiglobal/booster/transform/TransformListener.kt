package com.didiglobal.booster.transform

/**
 * Represents the transform lifecycle listener
 *
 * @author johnsonlee
 */
interface TransformListener {

    /**
     * The scope of inputs this transformer requires, [TransformScope.PROGRAM] by default.
     * Transformers which transform each class independently should declare [TransformScope.CLASS]
     * to be applied with AGP instrumentation, see [TransformScope]
     */
    val scope: TransformScope
        get() = TransformScope.PROGRAM

    fun onPreTransform(context: TransformContext) {}

    fun onPostTransform(context: TransformContext) {}

}
