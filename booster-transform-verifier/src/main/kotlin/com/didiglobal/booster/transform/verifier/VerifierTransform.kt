package com.didiglobal.booster.transform.verifier

import com.didiglobal.booster.annotations.Priority
import com.didiglobal.booster.transform.TransformContext
import com.didiglobal.booster.transform.asm.ClassTransformer
import com.google.auto.service.AutoService
import org.objectweb.asm.Opcodes
import org.objectweb.asm.Type
import org.objectweb.asm.tree.ClassNode
import org.objectweb.asm.tree.analysis.Analyzer
import org.objectweb.asm.tree.analysis.SimpleVerifier

/**
 * Represents a transformer for bytecode verifying
 *
 * @author johnsonlee
 */
@Priority(Int.MAX_VALUE)
@AutoService(ClassTransformer::class)
class VerifierTransform : ClassTransformer {

    override fun transform(context: TransformContext, klass: ClassNode): ClassNode {
        val superType = klass.superName?.let { Type.getObjectType(it) }
        val interfaces: List<Type> = klass.interfaces.map { Type.getObjectType(it) }
        val verifier = SimpleVerifier(Type.getObjectType(klass.name), superType, interfaces, (klass.access and Opcodes.ACC_INTERFACE) != 0).apply {
            setClassLoader(context.klassPool.classLoader)
        }
        val analyzer = Analyzer(verifier)

        klass.methods.forEach { method ->
            analyzer.analyze(klass.name, method)
        }

        return klass
    }

}