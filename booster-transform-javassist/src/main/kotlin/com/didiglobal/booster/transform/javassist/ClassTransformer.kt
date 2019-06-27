package com.didiglobal.booster.transform.javassist

import com.didiglobal.booster.transform.TransformContext
import com.didiglobal.booster.transform.TransformListener
import javassist.CtClass

/**
 * Represents class transformer
 *
 * @author johnsonlee
 */
interface ClassTransformer : TransformListener {

    /**
     * Transform the specified class node
     *
     * @param context The transform context
     * @param klass The class node to be transformed
     * @return The transformed class node
     */
    fun transform(context: TransformContext, klass: CtClass) = klass

}
