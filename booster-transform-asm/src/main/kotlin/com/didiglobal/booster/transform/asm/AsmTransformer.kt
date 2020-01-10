package com.didiglobal.booster.transform.asm

import com.didiglobal.booster.annotations.Priority
import com.didiglobal.booster.transform.TransformContext
import com.didiglobal.booster.transform.Transformer
import com.google.auto.service.AutoService
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.tree.ClassNode
import java.lang.management.ManagementFactory
import java.lang.management.ThreadMXBean
import java.util.ServiceLoader

/**
 * Represents bytecode transformer using ASM
 *
 * @author johnsonlee
 */
@AutoService(Transformer::class)
class AsmTransformer : Transformer {

    private val threadMxBean = ManagementFactory.getThreadMXBean()

    private val durations = mutableMapOf<ClassTransformer, Long>()

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
                threadMxBean.sumCpuTime(transformer) {
                    transformer.transform(context, klass)
                }
            }.accept(writer)
        }.toByteArray()
    }

    override fun onPreTransform(context: TransformContext) {
        transformers.forEach { transformer ->
            threadMxBean.sumCpuTime(transformer) {
                transformer.onPreTransform(context)
            }
        }
    }

    override fun onPostTransform(context: TransformContext) {
        transformers.forEach { transformer ->
            threadMxBean.sumCpuTime(transformer) {
                transformer.onPostTransform(context)
            }
        }

        val w1 = durations.keys.map {
            it.javaClass.name.length
        }.max() ?: 20
        durations.forEach { (transformer, ns) ->
            println("${transformer.javaClass.name.padEnd(w1 + 1)}: ${ns / 1000000} ms")
        }
    }

    private fun <R> ThreadMXBean.sumCpuTime(transformer: ClassTransformer, action: () -> R): R {
        val ct0 = currentThreadCpuTime
        val result = action()
        val ct1 = currentThreadCpuTime
        durations[transformer] = durations.getOrDefault(transformer, 0) + (ct1 - ct0)
        return result
    }
}
