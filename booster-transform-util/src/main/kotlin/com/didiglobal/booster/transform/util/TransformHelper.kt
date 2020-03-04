package com.didiglobal.booster.transform.util

import com.didiglobal.booster.build.AndroidSdk
import com.didiglobal.booster.kotlinx.file
import com.didiglobal.booster.transform.AbstractTransformContext
import com.didiglobal.booster.transform.Transformer
import com.didiglobal.booster.util.search
import java.io.File
import java.util.UUID

private val TMPDIR = File(System.getProperty("java.io.tmpdir"))

/**
 * Utility class for JAR or class file transforming
 *
 * @author johnsonlee
 */
class TransformHelper(val input: File, vararg val transformers: Transformer, val output: File = TMPDIR, val apiLevel: Int = AndroidSdk.findPlatform(), val applicationId: String = UUID.randomUUID().toString()) {

    fun transform() {
        val jars = input.search { it.extension == "jar" }
        val classes = input.search { it.extension == "class" }
        val context = object : AbstractTransformContext(
                applicationId,
                listOf(AndroidSdk.getLocation().file("platforms", "android-${apiLevel}", "android.jar")),
                jars,
                jars
        ) {
            override val projectDir: File = output
        }

        transformers.forEach {
            it.onPreTransform(context)
        }

        (jars + classes).forEach {
            it.transform(File(output, it.name)) { bytecode ->
                transformers.fold(bytecode) { bytes, transformer ->
                    transformer.transform(context, bytes)
                }
            }
        }

        transformers.forEach {
            it.onPostTransform(context)
        }

    }

}

