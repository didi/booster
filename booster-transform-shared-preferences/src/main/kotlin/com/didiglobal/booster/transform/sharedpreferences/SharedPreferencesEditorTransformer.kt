package com.didiglobal.booster.transform.sharedpreferences

import com.didiglobal.booster.kotlinx.asIterable
import com.didiglobal.booster.kotlinx.file
import com.didiglobal.booster.kotlinx.touch
import com.didiglobal.booster.transform.TransformContext
import com.didiglobal.booster.transform.asm.ClassTransformer
import com.didiglobal.booster.transform.shared.preferences.Build
import com.google.auto.service.AutoService
import jdk.internal.org.objectweb.asm.Opcodes
import org.objectweb.asm.tree.ClassNode
import org.objectweb.asm.tree.MethodInsnNode
import org.objectweb.asm.tree.MethodNode
import java.io.PrintWriter

/**
 * Represents a transformer for *SharedPreferences* optimization
 *
 * @author johnsonlee
 */
@AutoService(ClassTransformer::class)
class SharedPreferencesEditorTransformer : ClassTransformer {

    private lateinit var logger: PrintWriter

    override fun onPreTransform(context: TransformContext) {
        this.logger = context.reportsDir.file(Build.ARTIFACT).file(context.name).file("report.txt").touch().printWriter()
    }

    override fun onPostTransform(context: TransformContext) {
        this.logger.close()
    }

    override fun transform(context: TransformContext, klass: ClassNode): ClassNode {
        if (klass.name == SHADOW_EDITOR) {
            return klass
        }

        klass.methods.forEach { method ->
            method.instructions?.iterator()?.asIterable()?.filterIsInstance(MethodInsnNode::class.java)?.filter {
                it.opcode == Opcodes.INVOKEINTERFACE && it.owner == SHARED_PREFERENCES_EDITOR
            }?.forEach { invoke ->
                when ("${invoke.name}${invoke.desc}") {
                    "commit()Z" -> if (Opcodes.POP == invoke.next?.opcode) {
                        // if the return value of commit() does not used
                        // use asynchronous commit() instead
                        invoke.optimize(klass, method)
                        method.instructions.remove(invoke.next)
                    }
                    "apply()V" -> invoke.optimize(klass, method)
                }
            }
        }
        return klass
    }

    private fun MethodInsnNode.optimize(klass: ClassNode, method: MethodNode) {
        logger.println(" * ${this.owner}.${this.name}${this.desc} => $SHADOW_EDITOR.apply(L$SHARED_PREFERENCES_EDITOR;)V: ${klass.name}.${method.name}${method.desc}")
        this.itf = false
        this.owner = SHADOW_EDITOR
        this.name = "apply"
        this.opcode = Opcodes.INVOKESTATIC
        this.desc = "(L$SHARED_PREFERENCES_EDITOR;)V"
    }

}

private const val SHARED_PREFERENCES_EDITOR = "android/content/SharedPreferences\$Editor"

private const val SHADOW_EDITOR = "com/didiglobal/booster/instrument/ShadowEditor"
