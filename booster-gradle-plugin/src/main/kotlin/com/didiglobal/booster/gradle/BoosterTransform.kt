package com.didiglobal.booster.gradle

import com.android.build.api.transform.QualifiedContent
import com.android.build.api.transform.Transform
import com.android.build.api.transform.TransformInvocation
import com.android.build.gradle.BaseExtension
import com.android.build.gradle.internal.pipeline.TransformManager
import com.didiglobal.booster.annotations.Priority
import com.didiglobal.booster.gradle.internal.BoosterTransformV34
import com.didiglobal.booster.transform.AbstractKlassPool
import org.gradle.api.Project

/**
 * Represents the transform base
 *
 * @author johnsonlee
 */
open class BoosterTransform protected constructor(val project: Project) : Transform() {

    /*
     * Preload transformers as List to fix NoSuchElementException caused by ServiceLoader in parallel mode
     */
    internal val transformers = loadTransformers(project.buildscript.classLoader).sortedBy {
        it.javaClass.getAnnotation(Priority::class.java)?.value ?: 0
    }

    internal val verifyEnabled = project.getProperty(OPT_TRANSFORM_VERIFY, false)

    private val android: BaseExtension = project.getAndroid()

    private lateinit var androidKlassPool: AbstractKlassPool

    init {
        project.afterEvaluate {
            androidKlassPool = object : AbstractKlassPool(android.bootClasspath) {}
        }
    }

    val bootKlassPool: AbstractKlassPool
        get() = androidKlassPool

    override fun getName() = "booster"

    override fun isIncremental() = !verifyEnabled

    override fun isCacheable() = !verifyEnabled

    override fun getInputTypes(): MutableSet<QualifiedContent.ContentType> = TransformManager.CONTENT_CLASS

    override fun getScopes(): MutableSet<in QualifiedContent.Scope> = when {
        transformers.isEmpty() -> mutableSetOf()
        project.plugins.hasPlugin("com.android.library") -> SCOPE_PROJECT
        project.plugins.hasPlugin("com.android.application") -> SCOPE_FULL_PROJECT
        project.plugins.hasPlugin("com.android.dynamic-feature") -> SCOPE_FULL_WITH_FEATURES
        else -> TODO("Not an Android project")
    }

    override fun getReferencedScopes(): MutableSet<in QualifiedContent.Scope> = when {
        transformers.isEmpty() -> when {
            project.plugins.hasPlugin("com.android.library") -> SCOPE_PROJECT
            project.plugins.hasPlugin("com.android.application") -> SCOPE_FULL_PROJECT
            project.plugins.hasPlugin("com.android.dynamic-feature") -> SCOPE_FULL_WITH_FEATURES
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

        fun newInstance(project: Project): BoosterTransform = when {
            GTE_V3_4 -> BoosterTransformV34(project)
            else -> BoosterTransform(project)
        }

    }

}

/**
 * The option for transform outputs verifying, default is false
 */
private const val OPT_TRANSFORM_VERIFY = "booster.transform.verify"
