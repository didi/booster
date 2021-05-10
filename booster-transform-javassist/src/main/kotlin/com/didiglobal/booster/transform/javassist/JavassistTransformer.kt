package com.didiglobal.booster.transform.javassist

import com.didiglobal.booster.annotations.Priority
import com.didiglobal.booster.kotlinx.touch
import com.didiglobal.booster.transform.TransformContext
import com.didiglobal.booster.transform.Transformer
import com.didiglobal.booster.transform.util.diff
import com.google.auto.service.AutoService
import javassist.ClassPool
import java.io.ByteArrayOutputStream
import java.io.DataOutputStream
import java.io.File
import java.lang.management.ManagementFactory
import java.lang.management.ThreadMXBean
import java.util.ServiceLoader

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

    private val classLoader: ClassLoader

    private val transformers: Iterable<ClassTransformer>

    constructor() : this(Thread.currentThread().contextClassLoader)

    constructor(classLoader: ClassLoader = Thread.currentThread().contextClassLoader) : this(classLoader, ServiceLoader.load(ClassTransformer::class.java, classLoader).sortedBy {
        it.javaClass.getAnnotation(Priority::class.java)?.value ?: 0
    })

    constructor(classLoader: ClassLoader = Thread.currentThread().contextClassLoader, transformers: Iterable<ClassTransformer>) {
        this.classLoader = classLoader
        this.transformers = transformers
    }

    override fun onPreTransform(context: TransformContext) {
        this.pool.appendClassPath(context.bootClasspath.joinToString(File.pathSeparator) { it.canonicalPath })
        this.pool.appendClassPath(context.compileClasspath.joinToString(File.pathSeparator) { it.canonicalPath })

        this.transformers.forEach { transformer ->
            this.threadMxBean.sumCpuTime(transformer) {
                transformer.onPreTransform(context)
            }
        }
    }

    override fun transform(context: TransformContext, bytecode: ByteArray): ByteArray {
        val diffEnabled = context.getProperty("booster.transform.diff", false)
        return ByteArrayOutputStream().use { output ->
            bytecode.inputStream().use { input ->
                this.transformers.fold(this.pool.makeClass(input)) { a, transformer ->
                    this.threadMxBean.sumCpuTime(transformer) {
                        if (diffEnabled) {
                            val left = a.textify()
                            transformer.transform(context, a).also trans@{ b ->
                                val right = b.textify()
                                val diff = if (left == right) "" else left diff right
                                if (diff.isEmpty() || diff.isBlank()) {
                                    return@trans
                                }
                                transformer.getReport(context, "${a.name}.diff").touch().writeText(diff)
                            }
                        } else {
                            transformer.transform(context, a)
                        }
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
