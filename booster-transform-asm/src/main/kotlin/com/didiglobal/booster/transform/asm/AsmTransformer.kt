package com.didiglobal.booster.transform.asm

import com.didiglobal.booster.annotations.Priority
import com.didiglobal.booster.transform.TransformContext
import com.didiglobal.booster.transform.Transformer
import com.google.auto.service.AutoService
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.tree.ClassNode
import java.util.ServiceLoader

/**
 * Represents bytecode transformer using ASM
 *
 * @author johnsonlee
 */
@AutoService(Transformer::class)
class AsmTransformer : Transformer {

    /*
     * Preload transformers as List to fix NoSuchElementException caused by ServiceLoader in parallel mode
     */
    internal val transformers = ServiceLoader.load(ClassTransformer::class.java, javaClass.classLoader).sortedBy {
        it.javaClass.getAnnotation(Priority::class.java)?.value ?: 0
    }.toList()

    override fun transform(context: TransformContext, bytecode: ByteArray): ByteArray {
        return ClassWriter(ClassWriter.COMPUTE_MAXS).also { writer ->
            transformers.fold(ClassNode().also { klass ->
                ClassReader(bytecode).accept(klass, 0)
            }) { klass, transformer ->
                transformer.transform(context, klass)
            }.accept(writer)
        }.toByteArray()
    }

    override fun onPreTransform(context: TransformContext) {
        transformers.forEach {
            it.onPreTransform(context)
        }
    }

    override fun onPostTransform(context: TransformContext) {
        transformers.forEach {
            it.onPostTransform(context)
        }
    }
}
