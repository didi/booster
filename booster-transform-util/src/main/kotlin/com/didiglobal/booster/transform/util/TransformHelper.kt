package com.didiglobal.booster.transform.util

import com.didiglobal.booster.kotlinx.file
import com.didiglobal.booster.transform.AbstractTransformContext
import com.didiglobal.booster.transform.ArtifactManager
import com.didiglobal.booster.transform.TransformContext
import com.didiglobal.booster.transform.Transformer
import java.io.File
import java.util.UUID

private val TMPDIR = File(System.getProperty("java.io.tmpdir"))

/**
 * Utility class for JAR or class file transforming
 *
 * @param input The files to transform
 * @param platform The specific android platform location, such as ${ANDROID_HOME}/platforms/android-28
 * @param artifacts The artifact manager
 * @param applicationId An identifier for transform output
 * @param variant The variant name
 *
 * @author johnsonlee
 */
open class TransformHelper(
        val input: File,
        val platform: File,
        val artifacts: ArtifactManager = object : ArtifactManager {},
        val applicationId: String = UUID.randomUUID().toString(),
        val variant: String = "debug"
) {

    fun transform(output: File = TMPDIR, transformer: (TransformContext, ByteArray) -> ByteArray = { _, it -> it }) = transform(output, object : Transformer {
        override fun transform(context: TransformContext, bytecode: ByteArray) = transformer(context, bytecode)
    })

    fun transform(output: File = TMPDIR, vararg transformers: Transformer) {
        val self = this
        val inputs = if (this.input.isDirectory) this.input.listFiles()?.toList() ?: emptyList() else listOf(this.input)
        val classpath = inputs.filter {
            it.isDirectory || it.extension.run {
                equals("class", true) || equals("jar", true)
            }
        }
        val context = object : AbstractTransformContext(
                applicationId,
                variant,
                listOf(platform.file("android.jar"), platform.file("optional", "org.apache.http.legacy.jar")),
                classpath,
                classpath
        ) {
            override val projectDir = output
            override val artifacts = self.artifacts
        }

        transformers.forEach {
            it.onPreTransform(context)
        }

        inputs.forEach {
            it.transform(context.buildDir.file("transforms", it.name)) { bytecode ->
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

