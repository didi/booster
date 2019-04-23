package com.didiglobal.booster.transform.bugfix.toast

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
class ToastBugfixTransformer : ClassTransformer {

    override fun transform(context: TransformContext, klass: ClassNode): ClassNode {
        klass.methods.forEach { method ->
            method.instructions?.iterator()?.asIterable()?.filterIsInstance(MethodInsnNode::class.java)?.filter {
                it.owner == TOAST && it.name == "show" && it.desc == "()V"
            }?.forEach {
                it.owner = `TOAST'`
                it.desc = "(L$TOAST;)V"
                it.opcode = Opcodes.INVOKESTATIC
            }
        }
        return klass
    }

}

private const val TOAST = "android/widget/Toast"

private const val `TOAST'` = "com/didiglobal/booster/$TOAST"
