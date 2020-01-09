package com.didiglobal.booster.gradle

import com.android.build.api.transform.QualifiedContent
import com.android.build.api.transform.Transform
import com.android.build.api.transform.TransformInvocation
import com.android.build.gradle.BaseExtension
import com.android.build.gradle.internal.pipeline.TransformManager
import com.android.builder.model.AndroidProject
import com.didiglobal.booster.kotlinx.file
import com.didiglobal.booster.kotlinx.touch
import com.didiglobal.booster.transform.AbstractKlassPool
import org.gradle.api.Project
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

/**
 * Represents the transform base
 *
 * @author johnsonlee
 */
abstract class BoosterTransform(val project: Project) : Transform() {

    private val android: BaseExtension = project.getAndroid()

    internal val executor: ExecutorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors())

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

    final override fun transform(invocation: TransformInvocation?) {
        invocation?.let {
            BoosterTransformInvocation(it, this).apply {
                dumpInputs(this)

                if (isIncremental) {
                    onPreTransform(this)
                    doIncrementalTransform()
                } else {
                    buildDir.file(AndroidProject.FD_INTERMEDIATES, "transforms", "dexBuilder").let { dexBuilder ->
                        if (dexBuilder.exists()) {
                            dexBuilder.deleteRecursively()
                        }
                    }
                    outputProvider.deleteAll()
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

    private fun dumpInputs(invocation: BoosterTransformInvocation) {
        invocation.context.temporaryDir.file("inputs.txt").touch().printWriter().use { printer ->
            invocation.inputs.flatMap {
                it.jarInputs
            }.map {
                it.file
            }.forEach(printer::println)
        }
    }

}
