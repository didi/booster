package com.didiglobal.booster.transform.toast

import com.didiglobal.booster.kotlinx.asIterable
import com.didiglobal.booster.kotlinx.file
import com.didiglobal.booster.kotlinx.touch
import com.didiglobal.booster.transform.Klass
import com.didiglobal.booster.transform.TransformContext
import com.didiglobal.booster.transform.asm.ClassTransformer
import com.google.auto.service.AutoService
import org.objectweb.asm.Opcodes
import org.objectweb.asm.tree.ClassNode
import org.objectweb.asm.tree.MethodInsnNode
import org.objectweb.asm.tree.MethodNode
import java.io.PrintWriter

/**
 * Represents a class node transformer used to fix [bug: 30150688](https://android.googlesource.com/platform/frameworks/base/+/dc24f93)
 *
 * @author johnsonlee
 */
@AutoService(ClassTransformer::class)
class ToastTransformer : ClassTransformer {

    private lateinit var logger: PrintWriter

    override fun onPreTransform(context: TransformContext) {
        this.logger = context.reportsDir.file(Build.ARTIFACT).file(context.name).file("report.txt").touch().printWriter()
    }

    override fun onPostTransform(context: TransformContext) {
        this.logger.close()
    }

    override fun transform(context: TransformContext, klass: ClassNode): ClassNode {
        if (klass.name == SHADOW_TOAST) {
            return klass
        }

        klass.methods.forEach { method ->
            method.instructions?.iterator()?.asIterable()?.filterIsInstance(MethodInsnNode::class.java)?.filter {
                it.opcode == Opcodes.INVOKEVIRTUAL && it.name == "show" && it.desc == "()V" && (it.owner == TOAST || context.klassPool.get(TOAST).isAssignableFrom(it.owner))
            }?.forEach {
                it.optimize(klass, method)
            }
        }
        return klass
    }

    private fun MethodInsnNode.optimize(klass: ClassNode, method: MethodNode) {
        logger.println(" * ${this.owner}.${this.name}${this.desc} => $SHADOW_TOAST.apply(L$SHADOW_TOAST;)V: ${klass.name}.${method.name}${method.desc}")
        this.owner = SHADOW_TOAST
        this.name = "show"
        this.desc = "(L$TOAST;)V"
        this.opcode = Opcodes.INVOKESTATIC
        this.itf = false
    }

}

private const val TOAST = "android/widget/Toast"

private const val SHADOW_TOAST = "com/didiglobal/booster/instrument/ShadowToast"
