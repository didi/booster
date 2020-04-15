package com.didiglobal.booster.transform.javassist

import com.didiglobal.booster.annotations.Priority
import com.didiglobal.booster.build.BoosterServiceLoader
import com.didiglobal.booster.transform.TransformContext
import com.didiglobal.booster.transform.Transformer
import com.google.auto.service.AutoService
import javassist.ClassPool
import java.io.ByteArrayOutputStream
import java.io.DataOutputStream
import java.lang.management.ManagementFactory
import java.lang.management.ThreadMXBean

/**
 * Represents bytecode transformer using Javassist
 *
 * @author johnsonlee
 */
@AutoService(Transformer::class)
class JavassistTransformer : Transformer {

    private val pool = ClassPool()

    private val threadMxBean = ManagementFactory.getThreadMXBean()

    private val durations = mutableMapOf<ClassTransformer, Long>()

    internal val transformers: Collection<ClassTransformer>

    /*
     * Preload transformers as List to fix NoSuchElementException caused by ServiceLoader in parallel mode
     */
    constructor() : this(*BoosterServiceLoader.load(ClassTransformer::class.java, JavassistTransformer::class.java.classLoader).toList().toTypedArray())

    /**
     * For unit test only
     */
    constructor(vararg transformers: ClassTransformer) {
        this.transformers = transformers.sortedBy {
            it.javaClass.getAnnotation(Priority::class.java)?.value ?: 0
        }
    }

    override fun onPreTransform(context: TransformContext) {
        context.bootClasspath.forEach {
            this.pool.appendClassPath(it.absolutePath)
        }

        this.transformers.forEach { transformer ->
            this.threadMxBean.sumCpuTime(transformer) {
                transformer.onPreTransform(context)
            }
        }
    }

    override fun transform(context: TransformContext, bytecode: ByteArray): ByteArray {
        return ByteArrayOutputStream().use { output ->
            bytecode.inputStream().use { input ->
                this.transformers.fold(this.pool.makeClass(input)) { klass, transformer ->
                    this.threadMxBean.sumCpuTime(transformer) {
                        transformer.transform(context, klass)
                    }
                }.classFile.write(DataOutputStream(output))
            }
            output.toByteArray()
        }
    }

    override fun onPostTransform(context: TransformContext) {
        this.transformers.forEach {
            it.onPostTransform(context)
        }

        val w1 = this.durations.keys.map {
            it.javaClass.name.length
        }.max() ?: 20
        this.durations.forEach { (transformer, ns) ->
            println("${transformer.javaClass.name.padEnd(w1 + 1)}: ${ns / 1000000} ms")
        }
    }

    private fun <R> ThreadMXBean.sumCpuTime(transformer: ClassTransformer, action: () -> R): R {
        val ct0 = this.currentThreadCpuTime
        val result = action()
        val ct1 = this.currentThreadCpuTime
        durations[transformer] = durations.getOrDefault(transformer, 0) + (ct1 - ct0)
        return result
    }

}
