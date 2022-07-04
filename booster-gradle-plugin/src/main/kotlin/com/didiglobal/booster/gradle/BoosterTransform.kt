package com.didiglobal.booster.gradle

import com.android.build.api.transform.QualifiedContent
import com.android.build.api.transform.Transform
import com.android.build.api.transform.TransformInvocation
import com.android.build.gradle.internal.pipeline.TransformManager
import com.didiglobal.booster.gradle.internal.BoosterTransformV34
import org.gradle.api.Project

/**
 * Represents the transform base
 *
 * @author johnsonlee
 */
open class BoosterTransform protected constructor(
        internal val parameter: TransformParameter
) : Transform() {

    internal val verifyEnabled by lazy {
        parameter.properties[OPT_TRANSFORM_VERIFY]?.toString()?.toBoolean() ?: false
    }

    override fun getName() = parameter.name

    override fun isIncremental() = !verifyEnabled

    override fun isCacheable() = !verifyEnabled

    override fun getInputTypes(): MutableSet<QualifiedContent.ContentType> = TransformManager.CONTENT_CLASS

    override fun getScopes(): MutableSet<in QualifiedContent.Scope> = when {
        parameter.transformers.isEmpty() -> mutableSetOf()
        parameter.plugins.hasPlugin("com.android.library") -> SCOPE_PROJECT
        parameter.plugins.hasPlugin("com.android.application") -> SCOPE_FULL_PROJECT
        parameter.plugins.hasPlugin("com.android.dynamic-feature") -> SCOPE_FULL_WITH_FEATURES
        else -> TODO("Not an Android project")
    }

    override fun getReferencedScopes(): MutableSet<in QualifiedContent.Scope> = when {
        parameter.transformers.isEmpty() -> when {
            parameter.plugins.hasPlugin("com.android.library") -> SCOPE_PROJECT
            parameter.plugins.hasPlugin("com.android.application") -> SCOPE_FULL_PROJECT
            parameter.plugins.hasPlugin("com.android.dynamic-feature") -> SCOPE_FULL_WITH_FEATURES
            else -> TODO("Not an Android project")
        }
        else -> super.getReferencedScopes()
    }

    final override fun transform(invocation: TransformInvocation) {
        BoosterTransformInvocation(invocation, this).apply {
            if (isIncremental) {
                doIncrementalTransform()
            } else {
                outputProvider?.deleteAll()
                doFullTransform()
            }
        }
    }

    companion object {

        fun newInstance(project: Project, name: String = "booster"): BoosterTransform {
            val parameter = project.newTransformParameter(name)
            return when {
                GTE_V3_4 -> BoosterTransformV34(parameter)
                else -> BoosterTransform(parameter)
            }
        }

    }

}

/**
 * The option for transform outputs verifying, default is false
 */
private const val OPT_TRANSFORM_VERIFY = "booster.transform.verify"
