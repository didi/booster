package com.didiglobal.booster.transform.asm

import com.didiglobal.booster.annotations.Priority
import com.didiglobal.booster.transform.TransformContext
import com.didiglobal.booster.transform.Transformer
import com.google.auto.service.AutoService
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.tree.ClassNode
import java.io.File
import java.io.InputStream
import java.lang.management.ManagementFactory
import java.lang.management.ThreadMXBean
import java.util.ServiceLoader
import java.util.jar.JarFile

/**
 * Represents bytecode transformer using ASM
 *
 * @author johnsonlee
 */
@AutoService(Transformer::class)
class AsmTransformer : Transformer {

    private val threadMxBean = ManagementFactory.getThreadMXBean()

    private val durations = mutableMapOf<ClassTransformer, Long>()

    private val classLoader: ClassLoader

    internal val transformers: Iterable<ClassTransformer>

    constructor() : this(Thread.currentThread().contextClassLoader)

    constructor(classLoader: ClassLoader = Thread.currentThread().contextClassLoader) : this(ServiceLoader.load(ClassTransformer::class.java, classLoader).sortedBy {
        it.javaClass.getAnnotation(Priority::class.java)?.value ?: 0
    }, classLoader)

    constructor(transformers: Iterable<ClassTransformer>, classLoader: ClassLoader = Thread.currentThread().contextClassLoader) {
        this.classLoader = classLoader
        this.transformers = transformers
    }

    override fun onPreTransform(context: TransformContext) {
        this.transformers.forEach { transformer ->
            this.threadMxBean.sumCpuTime(transformer) {
                transformer.onPreTransform(context)
            }
        }
    }

    override fun transform(context: TransformContext, bytecode: ByteArray): ByteArray {
        return ClassWriter(ClassWriter.COMPUTE_MAXS).also { writer ->
            this.transformers.fold(ClassNode().also { klass ->
                ClassReader(bytecode).accept(klass, 0)
            }) { klass, transformer ->
                this.threadMxBean.sumCpuTime(transformer) {
                    transformer.transform(context, klass)
                }
            }.accept(writer)
        }.toByteArray()
    }

    override fun onPostTransform(context: TransformContext) {
        this.transformers.forEach { transformer ->
            this.threadMxBean.sumCpuTime(transformer) {
                transformer.onPostTransform(context)
            }
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

fun JarFile.transform(name: String, consumer: (ClassNode) -> Unit) = getJarEntry(name)?.let { entry ->
    getInputStream(entry).use { input ->
        consumer(input.asClassNode())
    }
}

fun ByteArray.asClassNode() = ClassNode().also { klass ->
    ClassReader(this).accept(klass, 0)
}

fun InputStream.asClassNode() = readBytes().asClassNode()

fun File.asClassNode(): ClassNode = readBytes().asClassNode()
