package com.didiglobal.booster.transform.sharedpreferences

import com.didiglobal.booster.kotlinx.GREEN
import com.didiglobal.booster.kotlinx.RESET
import com.didiglobal.booster.kotlinx.asIterable
import com.didiglobal.booster.transform.TransformContext
import com.didiglobal.booster.transform.asm.ClassTransformer
import com.google.auto.service.AutoService
import jdk.internal.org.objectweb.asm.Opcodes
import org.objectweb.asm.tree.ClassNode
import org.objectweb.asm.tree.MethodInsnNode
import org.objectweb.asm.tree.MethodNode

/**
 * Represents a transformer for *SharedPreferences* bug fixing
 *
 * @author johnsonlee
 */
@AutoService(ClassTransformer::class)
class SharedPreferencesEditorTransformer : ClassTransformer {

    override fun transform(context: TransformContext, klass: ClassNode): ClassNode {
        if (klass.name == SHADOW_EDITOR) {
            return klass
        }

        klass.methods.forEach { method ->
            method.instructions?.iterator()?.asIterable()?.filterIsInstance(MethodInsnNode::class.java)?.filter {
                it.opcode == Opcodes.INVOKEINTERFACE && it.owner == SHARED_PREFERENCES_EDITOR
            }?.forEach { invoke ->
                when ("${invoke.name}${invoke.desc}") {
                    "commit()Z" -> {
                        // if the return value of commit() does not used
                        // use asynchronous commit() instead
                        if (Opcodes.POP == invoke.next?.opcode) {
                            optimize(invoke, klass, method)
                            method.instructions.remove(invoke.next)
                        }
                    }
                    "apply()V" -> {
                        optimize(invoke, klass, method)
                    }
                }
            }
        }
        return klass
    }

}

private fun optimize(invoke: MethodInsnNode, klass: ClassNode, method: MethodNode) {
    println(" * $GREEN${invoke.owner}.${invoke.name}${invoke.desc}$RESET => $GREEN$SHADOW_EDITOR.apply(L$SHARED_PREFERENCES_EDITOR;)V$RESET: ${klass.name}.${method.name}${method.desc}")
    invoke.apply {
        itf = false
        owner = SHADOW_EDITOR
        name = "apply"
        opcode = Opcodes.INVOKESTATIC
        desc = "(L$SHARED_PREFERENCES_EDITOR;)V"
    }
}

private const val SHARED_PREFERENCES_EDITOR = "android/content/SharedPreferences\$Editor"

private const val SHADOW_EDITOR = "com/didiglobal/booster/instrument/ShadowEditor"
