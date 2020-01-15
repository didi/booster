package com.didiglobal.booster.gradle

import com.android.build.api.transform.QualifiedContent
import com.android.build.api.transform.Transform
import com.android.build.api.transform.TransformInvocation
import com.android.build.gradle.BaseExtension
import com.android.build.gradle.internal.pipeline.TransformManager
import com.android.builder.model.AndroidProject
import com.didiglobal.booster.kotlinx.file
import com.didiglobal.booster.transform.AbstractKlassPool
import com.didiglobal.booster.transform.Transformer
import org.gradle.api.Project
import java.util.ServiceLoader
import java.util.concurrent.TimeUnit

/**
 * Represents the transform base
 *
 * @author johnsonlee
 */
abstract class BoosterTransform(val project: Project) : Transform() {

    /*
     * Preload transformers as List to fix NoSuchElementException caused by ServiceLoader in parallel mode
     */
    internal val transformers = ServiceLoader.load(Transformer::class.java, javaClass.classLoader).toList()

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

    override fun isIncremental() = true

    override fun getInputTypes(): MutableSet<QualifiedContent.ContentType> = TransformManager.CONTENT_CLASS

    override fun getScopes(): MutableSet<in QualifiedContent.Scope> = mutableSetOf()

    final override fun transform(invocation: TransformInvocation) {
        BoosterTransformInvocation(invocation, this).apply {
            if (isIncremental) {
                onPreTransform(this)
                doIncrementalTransform()
            } else {
                buildDir.file(AndroidProject.FD_INTERMEDIATES, "transforms", "dexBuilder").deleteRecursively()
                outputProvider?.deleteAll()
                onPreTransform(this)
                doFullTransform()
            }

            this.onPostTransform(this)
        }.executor.apply {
            shutdown()
            awaitTermination(1, TimeUnit.MINUTES)
        }
    }

}
