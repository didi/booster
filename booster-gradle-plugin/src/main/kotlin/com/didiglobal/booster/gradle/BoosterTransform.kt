package com.didiglobal.booster.gradle

import com.android.build.api.transform.QualifiedContent
import com.android.build.api.transform.Transform
import com.android.build.api.transform.TransformInvocation
import com.android.build.gradle.BaseExtension
import com.android.build.gradle.internal.pipeline.TransformManager
import com.android.builder.model.AndroidProject
import com.didiglobal.booster.kotlinx.file
import com.didiglobal.booster.kotlinx.touch
import org.gradle.api.Project
import java.net.URLClassLoader
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.jar.JarFile

/**
 * Represents the transform base
 *
 * @author johnsonlee
 */
abstract class BoosterTransform(val project: Project) : Transform() {

    private val android: BaseExtension = project.getAndroid()

    internal val executor: ExecutorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors())

    private lateinit var androidClassLoader: ClassLoader

    init {
        project.afterEvaluate {
            androidClassLoader = URLClassLoader(android.bootClasspath.map { it.toURI().toURL() }.toTypedArray())
            android.bootClasspath.filter {
                it.extension == "jar"
            }.parallelStream().map {
                JarFile(it)
            }.forEach {
                it.entries().asSequence().filter { entry ->
                    entry.name.substringAfterLast(".") == "class"
                }.forEach { entry ->
                    executor.execute {
                        androidClassLoader.loadClass(entry.name.substringBeforeLast(".").replace('/', '.'))
                    }
                }
            }
        }
    }

    val bootClassLoader: ClassLoader
        get() = androidClassLoader

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
