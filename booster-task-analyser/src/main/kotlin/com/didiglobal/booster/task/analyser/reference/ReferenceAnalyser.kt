package com.didiglobal.booster.task.analyser.reference

import com.android.build.gradle.AppExtension
import com.android.build.gradle.BaseExtension
import com.android.build.gradle.LibraryExtension
import com.android.build.gradle.api.BaseVariant
import com.didiglobal.booster.cha.ClassSet
import com.didiglobal.booster.gradle.dependencies
import com.didiglobal.booster.gradle.getAndroid
import com.didiglobal.booster.gradle.getTaskName
import com.didiglobal.booster.gradle.isAndroid
import com.didiglobal.booster.gradle.isJavaLibrary
import com.didiglobal.booster.gradle.javaCompilerTaskProvider
import com.didiglobal.booster.graph.Graph
import com.didiglobal.booster.kotlinx.NCPU
import com.didiglobal.booster.kotlinx.green
import com.didiglobal.booster.kotlinx.yellow
import com.didiglobal.booster.task.analyser.AsmClassFileParser
import org.gradle.api.Project
import org.gradle.api.artifacts.component.ComponentArtifactIdentifier
import org.gradle.api.artifacts.component.ProjectComponentIdentifier
import org.gradle.api.plugins.JavaPlugin
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
import java.io.File
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger

class ReferenceAnalyser(
        private val project: Project,
        private val variant: String
) {

    private val upstreamClassSets = project.upstreamArtifacts.associateWith {
        when (val id = it.id.componentIdentifier) {
            is ProjectComponentIdentifier -> project.rootProject.project(id.projectPath).classSets
            else -> mapOf(DEFAULT_VARIANT to ClassSet.from(it.file, AsmClassFileParser))
        }
    }

    private val upstreamClassToVariant = upstreamClassSets.values.map { upstream ->
        upstream.map { (variant, classSet) ->
            classSet.map { klass ->
                klass.name to variant
            }
        }
    }.flatten().flatten().toMap()

    fun analyse(): Graph<ReferenceNode> {
        val executor = Executors.newFixedThreadPool(NCPU)
        val graphs = ConcurrentHashMap<Reference, Graph.Builder<ReferenceNode>>()

        try {
            val classes = project.classSets.map { (variant, classSet) ->
                classSet.map {
                    it to variant
                }
            }.flatten()
            val index = AtomicInteger(0)
            val count = classes.size
            classes.map { (klass, variant) ->
                val edge = { to: ReferenceNode ->
                    graphs.getOrPut(Reference(klass.name, variant)) {
                        Graph.Builder()
                    }.addEdge(ReferenceNode(this.project.name, variant, klass.name), to)
                }
                val av = AnnotationAnalyser(edge)
                val sv = SignatureAnalyser(edge)
                val fv = FieldAnalyser(av, edge)
                val mv = MethodAnalyser(av, sv, edge)
                executor.submit<Pair<ClassNode, Long>> {
                    val t0 = System.currentTimeMillis()
                    klass.accept(ClassAnalyser(klass, av, fv, mv, sv, edge))
                    klass to (System.currentTimeMillis() - t0)
                }
            }.forEach {
                val (klass, duration) = it.get()
                println("${green(String.format("%3d%%", index.incrementAndGet() * 100 / count))} Analyse class ${klass.name} in ${yellow(duration)} ms")
            }
        } finally {
            executor.shutdown()
            executor.awaitTermination(1, TimeUnit.MINUTES)
        }

        return graphs.entries.filter {
            it.key.variant == variant
        }.fold(Graph.Builder<ReferenceNode>()) { acc, (_, builder) ->
            builder.build().forEach { edge ->
                acc.addEdge(edge)
            }
            acc
        }.build()
    }

    private fun analyse(type: Type, edge: (ReferenceNode) -> Graph.Builder<ReferenceNode>) {
        analyse(listOf(type), edge)
    }

    private fun analyse(types: Array<Type>, edge: (ReferenceNode) -> Graph.Builder<ReferenceNode>) {
        analyse(types.toList(), edge)
    }

    private fun analyse(types: Iterable<Type>, edge: (ReferenceNode) -> Graph.Builder<ReferenceNode>) {
        types.filter {
            upstreamClassToVariant.keys.contains(it.internalName)
        }.forEach {
            findReference(it.internalName)?.let { (artifact, _) ->
                edge(ReferenceNode(artifact.id.componentIdentifier.displayName, upstreamClassToVariant[it.internalName]
                        ?: "", it.internalName))
            }
        }
    }

    private fun findReference(owner: String) = upstreamClassSets.entries.find { (_, classSets) ->
        classSets.entries.find { (_, classSet) ->
            classSet.contains(owner)
        } != null
    }


    private inner class ClassAnalyser(
            cv: ClassVisitor,
            private val av: AnnotationVisitor,
            private val fv: FieldVisitor,
            private val mv: MethodVisitor,
            private val sv: SignatureVisitor,
            private val edge: (ReferenceNode) -> Graph.Builder<ReferenceNode>
    ) : ClassVisitor(Opcodes.ASM7, cv) {

        override fun visit(version: Int, access: Int, name: String?, signature: String?, superName: String?, interfaces: Array<out String>?) {
            superName?.let {
                analyse(Type.getObjectType(it), edge)
            }
            interfaces?.forEach {
                analyse(Type.getObjectType(it), edge)
            }
            signature?.let(::SignatureReader)?.accept(sv)
            super.visit(version, access, name, signature, superName, interfaces)
        }

        override fun visitAnnotation(descriptor: String, visible: Boolean): AnnotationVisitor {
            analyse(Type.getType(descriptor), edge)
            return av
        }

        override fun visitTypeAnnotation(typeRef: Int, typePath: TypePath, descriptor: String, visible: Boolean): AnnotationVisitor {
            analyse(Type.getType(descriptor), edge)
            return av
        }

        override fun visitField(access: Int, name: String, descriptor: String, signature: String?, value: Any?): FieldVisitor {
            analyse(Type.getType(descriptor), edge)
            if (value is Type) {
                analyse(value, edge)
            }
            signature?.let(::SignatureReader)?.acceptType(sv)
            return fv
        }

        override fun visitMethod(access: Int, name: String?, descriptor: String, signature: String?, exceptions: Array<out String>?): MethodVisitor {
            analyse(Type.getArgumentTypes(descriptor), edge)
            analyse(Type.getReturnType(descriptor), edge)
            signature?.let(::SignatureReader)?.accept(sv)
            exceptions?.forEach {
                analyse(Type.getObjectType(it), edge)
            }
            return mv
        }

    }

    private inner class AnnotationAnalyser(
            private val edge: (ReferenceNode) -> Graph.Builder<ReferenceNode>
    ) : AnnotationVisitor(Opcodes.ASM7) {

        override fun visit(name: String?, value: Any?) {
            if (value is Type) {
                analyse(value, edge)
            }
        }

        override fun visitEnum(name: String?, descriptor: String?, value: String?) {
            descriptor?.let {
                analyse(Type.getType(it), edge)
            }
        }

        override fun visitAnnotation(name: String?, descriptor: String?): AnnotationVisitor {
            descriptor?.let {
                analyse(Type.getType(it), edge)
            }
            return this
        }

        override fun visitArray(name: String?): AnnotationVisitor = this
    }

    private inner class FieldAnalyser(
            private val av: AnnotationVisitor,
            private val edge: (ReferenceNode) -> Graph.Builder<ReferenceNode>
    ) : FieldVisitor(Opcodes.ASM7) {

        override fun visitAnnotation(descriptor: String?, visible: Boolean): AnnotationVisitor {
            descriptor?.let {
                analyse(Type.getType(it), edge)
            }
            return av
        }

        override fun visitTypeAnnotation(typeRef: Int, typePath: TypePath?, descriptor: String?, visible: Boolean): AnnotationVisitor {
            descriptor?.let {
                analyse(Type.getType(it), edge)
            }
            return av
        }

    }

    private inner class MethodAnalyser(
            private val av: AnnotationVisitor,
            private val sv: SignatureAnalyser,
            private val edge: (ReferenceNode) -> Graph.Builder<ReferenceNode>
    ) : MethodVisitor(Opcodes.ASM7) {

        override fun visitAnnotationDefault(): AnnotationVisitor = av

        override fun visitAnnotation(descriptor: String?, visible: Boolean): AnnotationVisitor {
            descriptor?.let {
                analyse(Type.getType(it), edge)
            }
            return av
        }

        override fun visitTypeAnnotation(typeRef: Int, typePath: TypePath?, descriptor: String?, visible: Boolean): AnnotationVisitor {
            descriptor?.let {
                analyse(Type.getType(it), edge)
            }
            return av
        }

        override fun visitParameterAnnotation(parameter: Int, descriptor: String?, visible: Boolean): AnnotationVisitor {
            descriptor?.let {
                analyse(Type.getType(it), edge)
            }
            return av
        }

        override fun visitTypeInsn(opcode: Int, type: String?) {
            type?.let {
                analyse(Type.getObjectType(it), edge)
            }
        }

        override fun visitFieldInsn(opcode: Int, owner: String?, name: String?, descriptor: String?) {
            owner?.let {
                analyse(Type.getObjectType(it), edge)
            }
            descriptor?.let {
                analyse(Type.getType(it), edge)
            }
        }

        override fun visitMethodInsn(opcode: Int, owner: String?, name: String?, descriptor: String?, isInterface: Boolean) {
            owner?.let {
                analyse(Type.getObjectType(it), edge)
            }
            descriptor?.let {
                analyse(Type.getReturnType(it), edge)
                analyse(Type.getArgumentTypes(it), edge)
            }
        }

        override fun visitInvokeDynamicInsn(name: String?, descriptor: String?, bootstrapMethodHandle: Handle?, vararg bootstrapMethodArguments: Any?) {
            descriptor?.let {
                analyse(Type.getReturnType(it), edge)
                analyse(Type.getArgumentTypes(it), edge)
            }
        }

        override fun visitLdcInsn(value: Any?) {
            if (value is Type) {
                analyse(value, edge)
            }
        }

        override fun visitMultiANewArrayInsn(descriptor: String?, numDimensions: Int) {
            descriptor?.let {
                analyse(Type.getType(it), edge)
            }
        }

        override fun visitInsnAnnotation(typeRef: Int, typePath: TypePath?, descriptor: String?, visible: Boolean): AnnotationVisitor {
            descriptor?.let {
                analyse(Type.getType(it), edge)
            }
            return av
        }

        override fun visitTryCatchBlock(start: Label?, end: Label?, handler: Label?, type: String?) {
            type?.let {
                analyse(Type.getObjectType(it), edge)
            }
        }

        override fun visitTryCatchAnnotation(typeRef: Int, typePath: TypePath?, descriptor: String?, visible: Boolean): AnnotationVisitor {
            descriptor?.let {
                analyse(Type.getType(it), edge)
            }
            return av
        }

        override fun visitLocalVariable(name: String?, descriptor: String?, signature: String?, start: Label?, end: Label?, index: Int) {
            descriptor?.let {
                analyse(Type.getType(it), edge)
            }
            signature?.let(::SignatureReader)?.acceptType(sv)
        }

        override fun visitLocalVariableAnnotation(typeRef: Int, typePath: TypePath?, start: Array<out Label>?, end: Array<out Label>?, index: IntArray?, descriptor: String?, visible: Boolean): AnnotationVisitor {
            descriptor?.let {
                analyse(Type.getType(it), edge)
            }
            return av
        }
    }

    private inner class SignatureAnalyser(
            private val edge: (ReferenceNode) -> Graph.Builder<ReferenceNode>
    ) : SignatureVisitor(Opcodes.ASM7) {

        override fun visitClassType(name: String) {
            analyse(Type.getObjectType(name), edge)
            super.visitClassType(name)
        }

    }

}

private data class Reference(val name: String, val variant: String)

internal data class Artifact(val id: ComponentArtifactIdentifier, val file: File)

private val Project.upstreamArtifacts: Set<Artifact>
    get() = when {
        isAndroid -> {
            when (val android = getAndroid<BaseExtension>()) {
                is LibraryExtension -> android.libraryVariants
                is AppExtension -> android.applicationVariants
                else -> emptyList<BaseVariant>()
            }.asSequence().map(BaseVariant::dependencies).flatten().distinctBy {
                it.id.componentIdentifier
            }.map {
                Artifact(it.id, it.file)
            }.toSet()
        }
        isJavaLibrary -> {
            configurations.getByName(JavaPlugin.RUNTIME_CLASSPATH_CONFIGURATION_NAME).resolvedConfiguration.resolvedArtifacts.distinctBy {
                it.id.componentIdentifier
            }.map {
                Artifact(it.id, it.file)
            }.toSet()
        }
        else -> emptySet()
    }

/**
 * Returns the [ClassSet] of all variants, the key is the variant name
 */
private val Project.classSets: Map<String, ClassSet<ClassNode, AsmClassFileParser>>
    get() = when {
        isAndroid -> when (val android = getAndroid<BaseExtension>()) {
            is LibraryExtension -> android.libraryVariants.associate { variant ->
                variant.name to tasks.named(variant.getTaskName(TASK_CREATE_FULL_JAR)).get().outputs.files.map {
                    ClassSet.from(it, AsmClassFileParser)
                }.let {
                    ClassSet.of(it)
                }
            }
            is AppExtension -> android.applicationVariants.associate { variant ->
                variant.name to variant.javaCompilerTaskProvider.get().outputs.files.map {
                    ClassSet.from(it, AsmClassFileParser)
                }.let {
                    ClassSet.of(it)
                }
            }
            else -> emptyMap()
        }
        isJavaLibrary -> {
            mapOf(DEFAULT_VARIANT to tasks.named(JavaPlugin.JAR_TASK_NAME).get().outputs.files.map {
                ClassSet.from(it, AsmClassFileParser)
            }.let {
                ClassSet.of(it)
            })
        }
        else -> emptyMap()
    }