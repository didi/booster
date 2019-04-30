package com.didiglobal.booster.transform.toast

import com.didiglobal.booster.kotlinx.GREEN
import com.didiglobal.booster.kotlinx.RESET
import com.didiglobal.booster.kotlinx.asIterable
import com.didiglobal.booster.transform.TransformContext
import com.didiglobal.booster.transform.asm.ClassTransformer
import com.google.auto.service.AutoService
import org.objectweb.asm.Opcodes
import org.objectweb.asm.tree.ClassNode
import org.objectweb.asm.tree.MethodInsnNode

/**
 * Represents a class node transformer used to fix [bug: 30150688](https://android.googlesource.com/platform/frameworks/base/+/dc24f93)
 *
 * @author johnsonlee
 */
@AutoService(ClassTransformer::class)
class ToastTransformer : ClassTransformer {

    override fun transform(context: TransformContext, klass: ClassNode): ClassNode {
        if (klass.name == SHADOW_TOAST) {
            return klass
        }

        klass.methods.forEach { method ->
            method.instructions?.iterator()?.asIterable()?.filterIsInstance(MethodInsnNode::class.java)?.filter {
                it.owner == TOAST && it.name == "show" && it.desc == "()V"
            }?.forEach { invoke ->
                println(" * $GREEN${invoke.owner}.${invoke.name}${invoke.desc}$RESET => $GREEN$SHADOW_TOAST.apply(L$SHADOW_TOAST;)V: ${klass.name}.${method.name}${method.desc}$RESET")
                invoke.apply {
                    owner = SHADOW_TOAST
                    name = "show"
                    desc = "(L$TOAST;)V"
                    opcode = Opcodes.INVOKESTATIC
                    itf = false
                }
            }
        }
        return klass
    }

}

private const val TOAST = "android/widget/Toast"

private const val SHADOW_TOAST = "com/didiglobal/booster/instrument/ShadowToast"
