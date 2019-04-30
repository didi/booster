package com.didiglobal.booster.transform.thread

import com.didiglobal.booster.kotlinx.GREEN
import com.didiglobal.booster.kotlinx.RESET
import com.didiglobal.booster.kotlinx.asIterable
import com.didiglobal.booster.transform.TransformContext
import com.didiglobal.booster.transform.asm.ClassTransformer
import com.didiglobal.booster.transform.asm.find
import com.didiglobal.booster.transform.asm.isInstanceOf
import com.google.auto.service.AutoService
import org.objectweb.asm.Opcodes
import org.objectweb.asm.tree.ClassNode
import org.objectweb.asm.tree.InsnNode
import org.objectweb.asm.tree.LdcInsnNode
import org.objectweb.asm.tree.MethodInsnNode
import org.objectweb.asm.tree.MethodNode
import org.objectweb.asm.tree.TypeInsnNode


@AutoService(ClassTransformer::class)
class ThreadTransformer : ClassTransformer {

    override fun transform(context: TransformContext, klass: ClassNode): ClassNode {
        if (klass.name.startsWith(SHADOW)) {
            return klass
        }

        klass.methods?.forEach { method ->
            method.instructions?.iterator()?.asIterable()?.forEach loop@{
                when (it.opcode) {
                    Opcodes.INVOKEVIRTUAL -> {
                        (it as MethodInsnNode).transformInvokeVirtual(context, klass, method)
                    }
                    Opcodes.INVOKESTATIC -> {
                        (it as MethodInsnNode).transformInvokeStatic(context, klass, method)
                    }
                    Opcodes.INVOKESPECIAL -> {
                        (it as MethodInsnNode).transformInvokeSpecial(context, klass, method)
                    }
                    Opcodes.NEW -> {
                        (it as TypeInsnNode).transform(context, klass, method)
                    }
                    Opcodes.ARETURN -> {
                        if (method.desc == "L$THREAD;") {
                            method.instructions.insertBefore(it, LdcInsnNode(makeThreadName(klass.name)))
                            method.instructions.insertBefore(it, MethodInsnNode(Opcodes.INVOKESTATIC, SHADOW_THREAD, "setThreadName", "(Ljava/lang/Thread;Ljava/lang/String;)Ljava/lang/Thread;", false))
                        }
                    }
                }
            }
        }
        return klass
    }

}

private fun MethodInsnNode.transformInvokeVirtual(context: TransformContext, klass: ClassNode, method: MethodNode) {
    if (context.klassPool.get(THREAD).isAssignableFrom(this.owner)) {
        when ("${this.name}${this.desc}") {
            "start()V" -> {
                method.instructions.insertBefore(this, LdcInsnNode(makeThreadName(klass.name)))
                method.instructions.insertBefore(this, MethodInsnNode(Opcodes.INVOKESTATIC, SHADOW_THREAD, "setThreadName", "(Ljava/lang/Thread;Ljava/lang/String;)Ljava/lang/Thread;", false))
                this.owner = THREAD
            }
            "setName(Ljava/lang/String;)V" -> {
                method.instructions.insertBefore(this, LdcInsnNode(makeThreadName(klass.name)))
                method.instructions.insertBefore(this, MethodInsnNode(Opcodes.INVOKESTATIC, SHADOW_THREAD, "makeThreadName", "(Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;", false))
                this.owner = THREAD
            }
        }
    }
}

private fun MethodInsnNode.transformInvokeSpecial(context: TransformContext, klass: ClassNode, method: MethodNode) {
    if (this.owner == THREAD && this.name == "<init>") {
        when (this.desc) {
            "()V",
            "(Ljava/lang/Runnable;)V",
            "(Ljava/lang/ThreadGroup;Ljava/lang/Runnable;)V" -> {
                method.instructions.insertBefore(this, LdcInsnNode(makeThreadName(klass.name)))
                val r = this.desc.lastIndexOf(')')
                this.desc = "${this.desc.substring(0, r)}Ljava/lang/String;${this.desc.substring(r)}"
            }
            "(Ljava/lang/String;)V",
            "(Ljava/lang/ThreadGroup;Ljava/lang/String;)V",
            "(Ljava/lang/Runnable;Ljava/lang/String;)V",
            "(Ljava/lang/ThreadGroup;Ljava/lang/Runnable;Ljava/lang/String;)V" -> {
                method.instructions.insertBefore(this, LdcInsnNode(makeThreadName(klass.name)))
                method.instructions.insertBefore(this, MethodInsnNode(Opcodes.INVOKESTATIC, SHADOW_THREAD, "makeThreadName", "(Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;", false))
            }
            "(Ljava/lang/ThreadGroup;Ljava/lang/Runnable;Ljava/lang/String;J)V" -> {
                method.instructions.insertBefore(this, InsnNode(Opcodes.POP2)) // discard the last argument: stackSize
                method.instructions.insertBefore(this, LdcInsnNode(makeThreadName(klass.name)))
                method.instructions.insertBefore(this, MethodInsnNode(Opcodes.INVOKESTATIC, SHADOW_THREAD, "makeThreadName", "(Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;", false))
                this.desc = "(Ljava/lang/ThreadGroup;Ljava/lang/Runnable;Ljava/lang/String;)V"
            }
        }

    }
}

private fun MethodInsnNode.transformInvokeStatic(context: TransformContext, klass: ClassNode, method: MethodNode) {
    when (this.owner) {
        EXECUTORS -> {
            when (this.name) {
                "defaultThreadFactory" -> {
                    val r = this.desc.lastIndexOf(')')
                    this.owner = SHADOW_EXECUTORS
                    this.desc = "${this.desc.substring(0, r)}Ljava/lang/String;${this.desc.substring(r)}"
                    method.instructions.insertBefore(this, LdcInsnNode(makeThreadName(klass.name)))
                }
                "newCachedThreadPool",
                "newFixedThreadPool",
                "newSingleThreadExecutor",
                "newSingleThreadScheduledExecutor",
                "newScheduledThreadPool" -> {
                    val r = this.desc.lastIndexOf(')')
                    val desc = "${this.desc.substring(0, r)}Ljava/lang/String;${this.desc.substring(r)}"
                    this.owner = SHADOW_EXECUTORS
                    this.name = this.name.replace("new", "newOptimized")
                    this.desc = desc
                    method.instructions.insertBefore(this, LdcInsnNode(makeThreadName(klass.name)))
                }
            }
        }
    }

}

private fun TypeInsnNode.transform(context: TransformContext, klass: ClassNode, method: MethodNode) {
    when (this.desc) {
        /*-*/ HANDLER_THREAD -> this.transformWithName(context, klass, method, SHADOW_HANDLER_THREAD)
        /*---------*/ THREAD -> this.transformWithName(context, klass, method, SHADOW_THREAD)
        THREAD_POOL_EXECUTOR -> this.transformWithName(context, klass, method, SHADOW_THREAD_POOL_EXECUTOR, "Optimized")
        /*----------*/ TIMER -> this.transformWithName(context, klass, method, SHADOW_TIMER)
    }
}

private fun TypeInsnNode.transformWithName(context: TransformContext, klass: ClassNode, method: MethodNode, type: String, prefix: String = "") {
    this.find {
        it.opcode == Opcodes.INVOKESPECIAL
    }?.isInstanceOf(MethodInsnNode::class.java) { init ->
        if (this.desc == init.owner && "<init>" == init.name) {
            val name = "new${prefix.capitalize()}${this.desc.substring(this.desc.lastIndexOf('/') + 1)}"
            val desc = "${init.desc.substring(0, init.desc.lastIndexOf(')'))}Ljava/lang/String;)L${this.desc};"

            println(" * $GREEN${init.owner}.${init.name}${init.desc}$RESET => $GREEN$type.$name$desc$RESET: ${klass.name}.${method.name}${method.desc}")

            // replace NEW with INVOKESTATIC
            init.owner = type
            init.name = name
            init.desc = desc
            init.opcode = Opcodes.INVOKESTATIC
            init.itf = false
            // add name as last parameter
            method.instructions.insertBefore(init, LdcInsnNode(makeThreadName(klass.name)))

            // remove the next DUP of NEW
            val dup = this.next
            if (Opcodes.DUP == dup.opcode) {
                method.instructions.remove(dup)
            } else {
                TODO("Unexpected instruction 0x${dup.opcode.toString(16)}: ${klass.name}.${method.name}${method.desc}")
            }
            method.instructions.remove(this)
        }
    }
}

private fun makeThreadName(name: String) = MARK + name.replace('/', '.')

internal val MARK = "\u200B"

const val SHADOW = "com/didiglobal/booster/instrument/Shadow"
const val SHADOW_HANDLER_THREAD = "${SHADOW}HandlerThread"
const val SHADOW_THREAD = "${SHADOW}Thread"
const val SHADOW_TIMER = "${SHADOW}Timer"
const val SHADOW_EXECUTORS = "${SHADOW}Executors"
const val SHADOW_THREAD_POOL_EXECUTOR = "${SHADOW}ThreadPoolExecutor"

const val HANDLER_THREAD = "android/os/HandlerThread"
const val THREAD = "java/lang/Thread"
const val TIMER = "java/util/Timer"
const val EXECUTORS = "java/util/concurrent/Executors"
const val THREAD_POOL_EXECUTOR = "java/util/concurrent/ThreadPoolExecutor"
