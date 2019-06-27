package com.didiglobal.booster.transform.javassist

import com.didiglobal.booster.transform.TransformContext
import com.didiglobal.booster.transform.Transformer
import com.google.auto.service.AutoService
import javassist.ClassPool
import java.io.ByteArrayOutputStream
import java.io.DataOutputStream
import java.util.ServiceLoader

/**
 * Represents bytecode transformer using Javassist
 *
 * @author johnsonlee
 */
@AutoService(Transformer::class)
class JavassistTransformer : Transformer {

    /*
     * Preload transformers as List to fix NoSuchElementException caused by ServiceLoader in parallel mode
     */
    private val transformers = ServiceLoader.load(ClassTransformer::class.java, javaClass.classLoader).toList()

    private lateinit var pool: ClassPool

    override fun onPreTransform(context: TransformContext) {
        pool = ClassPool().apply {
            context.bootClasspath.forEach {
                appendClassPath(it.absolutePath)
            }
        }

        transformers.forEach {
            it.onPreTransform(context)
        }
    }

    override fun transform(context: TransformContext, bytecode: ByteArray) = ByteArrayOutputStream().use { output ->
        bytecode.inputStream().use { input ->
            transformers.fold(pool.makeClass(input)) { klass, transformer ->
                transformer.transform(context, klass)
            }.classFile.write(DataOutputStream(output))
        }
        output.toByteArray()!!
    }

    override fun onPostTransform(context: TransformContext) {
        transformers.forEach {
            it.onPostTransform(context)
        }
    }

}
