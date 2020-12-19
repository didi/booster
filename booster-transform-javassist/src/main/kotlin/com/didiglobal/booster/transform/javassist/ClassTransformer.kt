package com.didiglobal.booster.transform.javassist

import com.didiglobal.booster.transform.TransformContext
import com.didiglobal.booster.transform.TransformListener
import javassist.CtClass
import java.io.File

/**
 * Represents class transformer
 *
 * @author johnsonlee
 */
interface ClassTransformer : TransformListener {

    val name: String
        get() = javaClass.simpleName

    fun getReportDir(context: TransformContext): File = File(File(context.reportsDir, name), context.name)

    fun getReport(context: TransformContext, name: String): File = File(getReportDir(context), name)


    /**
     * Transform the specified class node
     *
     * @param context The transform context
     * @param klass The class node to be transformed
     * @return The transformed class node
     */
    fun transform(context: TransformContext, klass: CtClass) = klass

}
