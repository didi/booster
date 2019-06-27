package com.didiglobal.booster.gradle

import com.android.build.api.transform.QualifiedContent
import com.android.build.api.transform.Transform
import com.android.build.api.transform.TransformInvocation
import com.android.build.gradle.internal.pipeline.TransformManager
import com.android.builder.model.AndroidProject
import com.didiglobal.booster.kotlinx.file
import com.didiglobal.booster.kotlinx.touch
import java.io.File
import java.util.concurrent.TimeUnit

/**
 * Represents the transform base
 *
 * @author johnsonlee
 */
abstract class BoosterTransform : Transform() {

    override fun getName() = "booster"

    override fun isIncremental() = true

    override fun getInputTypes(): MutableSet<QualifiedContent.ContentType> = TransformManager.CONTENT_CLASS

    final override fun transform(invocation: TransformInvocation?) {
        invocation?.let {
            BoosterTransformInvocation(it).apply {
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
