package com.didiglobal.booster.cha.asm

import com.didiglobal.booster.graph.Graph
import com.didiglobal.booster.kotlinx.NCPU
import org.objectweb.asm.AnnotationVisitor
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.FieldVisitor
import org.objectweb.asm.Handle
import org.objectweb.asm.Label
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes
import org.objectweb.asm.Type
import org.objectweb.asm.TypePath
import org.objectweb.asm.signature.SignatureReader
import org.objectweb.asm.signature.SignatureVisitor
import org.objectweb.asm.tree.ClassNode
import java.lang.management.ManagementFactory
import java.time.Duration
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger

/**
 * Analyser for class reference analysing
 */
class ReferenceAnalyser(private val asm: Int = Opcodes.ASM7) {

    private val threadMxBean = ManagementFactory.getThreadMXBean()

    /**
     * Analyse the references from [origin] to [upstream]
     *
     * @param origin The origin classSet with component identifier
     * @param upstream The upstream classSets with component identifiers
     * @return the reference graph
     */
    @JvmOverloads
    fun analyse(
            origin: Pair<String, AsmClassSet>,
            vararg upstream: Pair<String, AsmClassSet>,
            onProgressUpdate: ProgressListener? = null
    ): Graph<Reference> = analyse(origin, upstream.toMap(), onProgressUpdate)

    /**
     * Analyse the references from [origin] to [upstream]
     *
     * @param origin The origin classSet with component identifier
     * @param upstream The upstream classSets with component identifiers
     * @return the reference graph
     */
    @JvmOverloads
    fun analyse(
            origin: Pair<String, AsmClassSet>,
            upstream: Iterable<Pair<String, AsmClassSet>>,
            onProgressUpdate: ProgressListener? = null
    ): Graph<Reference> = analyse(origin, upstream.toMap(), onProgressUpdate)

    /**
     * Analyse the references from [origin] to [upstream]
     *
     * @param origin The origin classSet with component identifier
     * @param upstream The upstream classSets with component identifiers
     * @return the reference graph
     */
    @JvmOverloads
    fun analyse(
            origin: Pair<String, AsmClassSet>,
            upstream: Map<String, AsmClassSet>,
            onProgressUpdate: ProgressListener? = null
    ): Graph<Reference> {
        val executor = Executors.newFixedThreadPool(NCPU)
        val graphs = ConcurrentHashMap<String, Graph.Builder<Reference>>()
        val (identifier, classes) = origin

        try {
            val index = AtomicInteger(0)
            val count = classes.size

            classes.map { klass ->
                val edge = { to: Reference ->
                    graphs.getOrPut(klass.name) {
                        Graph.Builder()
                    }.addEdge(Reference(identifier, klass.name), to)
                }
                executor.submit<Pair<ClassNode, Duration>> {
                    val t0 = threadMxBean.currentThreadCpuTime
                    analyse(klass, upstream, edge)
                    val t1 = threadMxBean.currentThreadCpuTime
                    klass to (Duration.ofNanos(t1 - t0))
                }
            }.forEach {
                val (klass, duration) = it.get()
                onProgressUpdate?.invoke(klass, index.incrementAndGet().toFloat() / count, duration)
            }
        } finally {
            executor.shutdown()
            executor.awaitTermination(1, TimeUnit.MINUTES)
        }

        return graphs.entries.fold(Graph.Builder<Reference>()) { acc, (_, builder) ->
            builder.build().forEach { edge ->
                acc.addEdge(edge)
            }
            acc
        }.build()
    }

    /**
     * Analyse the specific [klass] to find out the referenced classes
     */
    fun analyse(klass: ClassNode): Set<Type> {
        val types = mutableSetOf<Type>()
        val av = AnnotationAnalyser(types)
        val sv = SignatureAnalyser(types)
        val fv = FieldAnalyser(av, types)
        val mv = MethodAnalyser(av, sv, types)
        klass.accept(ClassAnalyser(klass, av, fv, mv, sv, types))
        return types
    }

    private fun Map<String, AsmClassSet>.findReference(owner: String): Map.Entry<String, AsmClassSet>? {
        return entries.find { (_, classes) ->
            classes.contains(owner)
        }
    }

    private fun analyse(klass: ClassNode, upstream: Map<String, AsmClassSet>, edge: (Reference) -> Graph.Builder<Reference>) {
        analyse(klass).forEach {
            upstream.findReference(it.internalName)?.let { (identifier, _) ->
                edge(Reference(identifier, it.internalName))
            }
        }
    }

    private inner class ClassAnalyser(
            cv: ClassVisitor,
            private val av: AnnotationVisitor,
            private val fv: FieldVisitor,
            private val mv: MethodVisitor,
            private val sv: SignatureVisitor,
            private val types: MutableSet<Type>
    ) : ClassVisitor(asm, cv) {

        override fun visit(version: Int, access: Int, name: String?, signature: String?, superName: String?, interfaces: Array<out String>?) {
            superName?.let {
                types += Type.getObjectType(it)
            }
            interfaces?.forEach {
                types += Type.getObjectType(it)
            }
            signature?.let(::SignatureReader)?.accept(sv)
            super.visit(version, access, name, signature, superName, interfaces)
        }

        override fun visitAnnotation(descriptor: String, visible: Boolean): AnnotationVisitor {
            types += Type.getType(descriptor)
            return av
        }

        override fun visitTypeAnnotation(typeRef: Int, typePath: TypePath, descriptor: String, visible: Boolean): AnnotationVisitor {
            types += Type.getType(descriptor)
            return av
        }

        override fun visitField(access: Int, name: String, descriptor: String, signature: String?, value: Any?): FieldVisitor {
            types += Type.getType(descriptor)
            if (value is Type) {
                types += value
            }
            signature?.let(::SignatureReader)?.acceptType(sv)
            return fv
        }

        override fun visitMethod(access: Int, name: String?, descriptor: String, signature: String?, exceptions: Array<out String>?): MethodVisitor {
            types += Type.getArgumentTypes(descriptor)
            types += Type.getReturnType(descriptor)
            signature?.let(::SignatureReader)?.accept(sv)
            exceptions?.forEach {
                types += Type.getObjectType(it)
            }
            return mv
        }

    }

    private inner class AnnotationAnalyser(
            private val types: MutableSet<Type>
    ) : AnnotationVisitor(asm) {

        override fun visit(name: String?, value: Any?) {
            if (value is Type) {
                types += value
            }
        }

        override fun visitEnum(name: String?, descriptor: String?, value: String?) {
            descriptor?.let {
                types += Type.getType(it)
            }
        }

        override fun visitAnnotation(name: String?, descriptor: String?): AnnotationVisitor {
            descriptor?.let {
                types += Type.getType(it)
            }
            return this
        }

        override fun visitArray(name: String?): AnnotationVisitor = this
    }

    private inner class FieldAnalyser(
            private val av: AnnotationVisitor,
            private val types: MutableSet<Type>
    ) : FieldVisitor(asm) {

        override fun visitAnnotation(descriptor: String?, visible: Boolean): AnnotationVisitor {
            descriptor?.let {
                types += Type.getType(it)
            }
            return av
        }

        override fun visitTypeAnnotation(typeRef: Int, typePath: TypePath?, descriptor: String?, visible: Boolean): AnnotationVisitor {
            descriptor?.let {
                types += Type.getType(it)
            }
            return av
        }

    }

    private inner class MethodAnalyser(
            private val av: AnnotationVisitor,
            private val sv: SignatureAnalyser,
            private val types: MutableSet<Type>
    ) : MethodVisitor(asm) {

        override fun visitAnnotationDefault(): AnnotationVisitor = av

        override fun visitAnnotation(descriptor: String?, visible: Boolean): AnnotationVisitor {
            descriptor?.let {
                types += Type.getType(it)
            }
            return av
        }

        override fun visitTypeAnnotation(typeRef: Int, typePath: TypePath?, descriptor: String?, visible: Boolean): AnnotationVisitor {
            descriptor?.let {
                types += Type.getType(it)
            }
            return av
        }

        override fun visitParameterAnnotation(parameter: Int, descriptor: String?, visible: Boolean): AnnotationVisitor {
            descriptor?.let {
                types += Type.getType(it)
            }
            return av
        }

        override fun visitTypeInsn(opcode: Int, type: String?) {
            type?.let {
                types += Type.getObjectType(it)
            }
        }

        override fun visitFieldInsn(opcode: Int, owner: String?, name: String?, descriptor: String?) {
            owner?.let {
                types += Type.getObjectType(it)
            }
            descriptor?.let {
                types += Type.getType(it)
            }
        }

        override fun visitMethodInsn(opcode: Int, owner: String?, name: String?, descriptor: String?, isInterface: Boolean) {
            owner?.let {
                types += Type.getObjectType(it)
            }
            descriptor?.let {
                types += Type.getReturnType(it)
                types += Type.getArgumentTypes(it)
            }
        }

        override fun visitInvokeDynamicInsn(name: String?, descriptor: String?, bootstrapMethodHandle: Handle?, vararg bootstrapMethodArguments: Any?) {
            descriptor?.let {
                types += Type.getReturnType(it)
                types += Type.getArgumentTypes(it)
            }
        }

        override fun visitLdcInsn(value: Any?) {
            if (value is Type) {
                types += value
            }
        }

        override fun visitMultiANewArrayInsn(descriptor: String?, numDimensions: Int) {
            descriptor?.let {
                types += Type.getType(it)
            }
        }

        override fun visitInsnAnnotation(typeRef: Int, typePath: TypePath?, descriptor: String?, visible: Boolean): AnnotationVisitor {
            descriptor?.let {
                types += Type.getType(it)
            }
            return av
        }

        override fun visitTryCatchBlock(start: Label?, end: Label?, handler: Label?, type: String?) {
            type?.let {
                types += Type.getObjectType(it)
            }
        }

        override fun visitTryCatchAnnotation(typeRef: Int, typePath: TypePath?, descriptor: String?, visible: Boolean): AnnotationVisitor {
            descriptor?.let {
                types += Type.getType(it)
            }
            return av
        }

        override fun visitLocalVariable(name: String?, descriptor: String?, signature: String?, start: Label?, end: Label?, index: Int) {
            descriptor?.let {
                types += Type.getType(it)
            }
            signature?.let(::SignatureReader)?.acceptType(sv)
        }

        override fun visitLocalVariableAnnotation(typeRef: Int, typePath: TypePath?, start: Array<out Label>?, end: Array<out Label>?, index: IntArray?, descriptor: String?, visible: Boolean): AnnotationVisitor {
            descriptor?.let {
                types += Type.getType(it)
            }
            return av
        }
    }

    private inner class SignatureAnalyser(
            private val types: MutableSet<Type>
    ) : SignatureVisitor(asm) {

        override fun visitClassType(name: String) {
            types += Type.getObjectType(name)
            super.visitClassType(name)
        }

    }

}