package com.didiglobal.booster.transform.thread

import com.didiglobal.booster.kotlinx.asIterable
import com.didiglobal.booster.kotlinx.file
import com.didiglobal.booster.kotlinx.touch
import com.didiglobal.booster.transform.ArtifactManager
import com.didiglobal.booster.transform.TransformContext
import com.didiglobal.booster.transform.asm.ClassTransformer
import com.didiglobal.booster.transform.asm.className
import com.didiglobal.booster.transform.asm.find
import com.didiglobal.booster.transform.asm.findAll
import com.didiglobal.booster.transform.asm.isInstanceOf
import com.didiglobal.booster.util.ComponentHandler
import com.google.auto.service.AutoService
import org.objectweb.asm.Opcodes
import org.objectweb.asm.tree.ClassNode
import org.objectweb.asm.tree.InsnNode
import org.objectweb.asm.tree.LdcInsnNode
import org.objectweb.asm.tree.MethodInsnNode
import org.objectweb.asm.tree.MethodNode
import org.objectweb.asm.tree.TypeInsnNode
import java.io.PrintWriter
import javax.xml.parsers.SAXParserFactory

/**
 * Represents a class transformer for multithreading optimization
 *
 * @author johnsonlee
 */
@AutoService(ClassTransformer::class)
class ThreadTransformer : ClassTransformer {

    private lateinit var logger: PrintWriter
    private val applications = mutableSetOf<String>()

    override fun onPreTransform(context: TransformContext) {
        val parser = SAXParserFactory.newInstance().newSAXParser()
        context.artifacts.get(ArtifactManager.MERGED_MANIFESTS).forEach { manifest ->
            val handler = ComponentHandler()
            parser.parse(manifest, handler)
            applications.addAll(handler.applications)
        }

        this.logger = context.reportsDir.file(Build.ARTIFACT).file(context.name).file("report.txt").touch().printWriter()
    }

    override fun onPostTransform(context: TransformContext) {
        this.logger.close()
    }

    override fun transform(context: TransformContext, klass: ClassNode): ClassNode {
        if (klass.name.startsWith(SHADOW)) {
            return klass
        }

        if (this.applications.contains(klass.className)) {
            optimizeAsyncTask(klass)
        }

        klass.methods?.forEach { method ->
            method.instructions?.iterator()?.asIterable()?.forEach loop@{
                when (it.opcode) {
                    Opcodes.INVOKEVIRTUAL -> (it as MethodInsnNode).transformInvokeVirtual(context, klass, method)
                    Opcodes.INVOKESTATIC -> (it as MethodInsnNode).transformInvokeStatic(context, klass, method)
                    Opcodes.INVOKESPECIAL -> (it as MethodInsnNode).transformInvokeSpecial(context, klass, method)
                    Opcodes.NEW -> (it as TypeInsnNode).transform(context, klass, method)
                    Opcodes.ARETURN -> if (method.desc == "L$THREAD;") {
                        method.instructions.insertBefore(it, LdcInsnNode(makeThreadName(klass.className)))
                        method.instructions.insertBefore(it, MethodInsnNode(Opcodes.INVOKESTATIC, SHADOW_THREAD, "setThreadName", "(Ljava/lang/Thread;Ljava/lang/String;)Ljava/lang/Thread;", false))
                        logger.println(" + $SHADOW_THREAD.makeThreadName(Ljava/lang/String;Ljava/lang/String;) @return: ${klass.name}.${method.name}${method.desc}")
                    }
                }
            }
        }
        return klass
    }

private fun MethodInsnNode.transformInvokeVirtual(context: TransformContext, klass: ClassNode, method: MethodNode) {
    if (context.klassPool.get(THREAD).isAssignableFrom(this.owner)) {
        when ("${this.name}${this.desc}") {
            "start()V" -> {
                method.instructions.insertBefore(this, LdcInsnNode(makeThreadName(klass.className)))
                method.instructions.insertBefore(this, MethodInsnNode(Opcodes.INVOKESTATIC, SHADOW_THREAD, "setThreadName", "(Ljava/lang/Thread;Ljava/lang/String;)Ljava/lang/Thread;", false))
                logger.println(" + $SHADOW_THREAD.makeThreadName(Ljava/lang/String;Ljava/lang/String;) => ${this.owner}.${this.name}${this.desc}: ${klass.name}.${method.name}${method.desc}")
                this.owner = THREAD
            }
            "setName(Ljava/lang/String;)V" -> {
                method.instructions.insertBefore(this, LdcInsnNode(makeThreadName(klass.className)))
                method.instructions.insertBefore(this, MethodInsnNode(Opcodes.INVOKESTATIC, SHADOW_THREAD, "makeThreadName", "(Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;", false))
                logger.println(" + $SHADOW_THREAD.makeThreadName(Ljava/lang/String;Ljava/lang/String;) => ${this.owner}.${this.name}${this.desc}: ${klass.name}.${method.name}${method.desc}")
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
                    method.instructions.insertBefore(this, LdcInsnNode(makeThreadName(klass.className)))
                    val r = this.desc.lastIndexOf(')')
                    val desc = "${this.desc.substring(0, r)}Ljava/lang/String;${this.desc.substring(r)}"
                    logger.println(" + $SHADOW_THREAD.makeThreadName(Ljava/lang/String;Ljava/lang/String;) => ${this.owner}.${this.name}${this.desc}: ${klass.name}.${method.name}${method.desc}")
                    logger.println(" * ${this.owner}.${this.name}${this.desc} => ${this.owner}.${this.name}$desc: ${klass.name}.${method.name}${method.desc}")
                    this.desc = desc
                }
                "(Ljava/lang/String;)V",
                "(Ljava/lang/ThreadGroup;Ljava/lang/String;)V",
                "(Ljava/lang/Runnable;Ljava/lang/String;)V",
                "(Ljava/lang/ThreadGroup;Ljava/lang/Runnable;Ljava/lang/String;)V" -> {
                    method.instructions.insertBefore(this, LdcInsnNode(makeThreadName(klass.className)))
                    method.instructions.insertBefore(this, MethodInsnNode(Opcodes.INVOKESTATIC, SHADOW_THREAD, "makeThreadName", "(Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;", false))
                    logger.println(" + $SHADOW_THREAD.makeThreadName(Ljava/lang/String;Ljava/lang/String;) => ${this.owner}.${this.name}${this.desc}: ${klass.name}.${method.name}${method.desc}")
                }
                "(Ljava/lang/ThreadGroup;Ljava/lang/Runnable;Ljava/lang/String;J)V" -> {
                    method.instructions.insertBefore(this, InsnNode(Opcodes.POP2)) // discard the last argument: stackSize
                    method.instructions.insertBefore(this, LdcInsnNode(makeThreadName(klass.className)))
                    method.instructions.insertBefore(this, MethodInsnNode(Opcodes.INVOKESTATIC, SHADOW_THREAD, "makeThreadName", "(Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;", false))
                    logger.println(" + $SHADOW_THREAD.makeThreadName(Ljava/lang/String;Ljava/lang/String;) => ${this.owner}.${this.name}${this.desc}: ${klass.name}.${method.name}${method.desc}")
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
                        val desc = "${this.desc.substring(0, r)}Ljava/lang/String;${this.desc.substring(r)}"
                        logger.println(" * ${this.owner}.${this.name}${this.desc} => $SHADOW_EXECUTORS.${this.name}$desc: ${klass.name}.${method.name}${method.desc}")
                        this.owner = SHADOW_EXECUTORS
                        this.desc = desc
                        method.instructions.insertBefore(this, LdcInsnNode(makeThreadName(klass.className)))
                    }
                    "newCachedThreadPool",
                    "newFixedThreadPool",
                    "newSingleThreadExecutor",
                    "newSingleThreadScheduledExecutor",
                    "newScheduledThreadPool" -> {
                        val r = this.desc.lastIndexOf(')')
                        val name = this.name.replace("new", "newOptimized")
                        val desc = "${this.desc.substring(0, r)}Ljava/lang/String;${this.desc.substring(r)}"
                        logger.println(" * ${this.owner}.${this.name}${this.desc} => $SHADOW_EXECUTORS.$name$desc: ${klass.name}.${method.name}${method.desc}")
                        this.owner = SHADOW_EXECUTORS
                        this.name = name
                        this.desc = desc
                        method.instructions.insertBefore(this, LdcInsnNode(makeThreadName(klass.className)))
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
        }?.isInstanceOf { init: MethodInsnNode ->
            if (this.desc == init.owner && "<init>" == init.name) {
                val name = "new${prefix.capitalize()}${this.desc.substringAfterLast('/')}"
                val desc = "${init.desc.substringBeforeLast(')')}Ljava/lang/String;)L${this.desc};"
                logger.println(" * ${init.owner}.${init.name}${init.desc} => $type.$name$desc: ${klass.name}.${method.name}${method.desc}")
                // replace NEW with INVOKESTATIC
                init.owner = type
                init.name = name
                init.desc = desc
                init.opcode = Opcodes.INVOKESTATIC
                init.itf = false
                // add name as last parameter
                method.instructions.insertBefore(init, LdcInsnNode(makeThreadName(klass.className)))

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

    private fun optimizeAsyncTask(klass: ClassNode) {
        val method = klass.methods?.find {
            "${it.name}${it.desc}" == "<clinit>()V"
        } ?: klass.defaultClinit.also {
            klass.methods.add(it)
        }

        method.instructions?.let { insn ->
            insn.findAll(Opcodes.RETURN, Opcodes.ATHROW).forEach {
                insn.insertBefore(it, MethodInsnNode(Opcodes.INVOKESTATIC, SHADOW_ASYNC_TASK, "optimizeAsyncTaskExecutor", "()V", false))
                logger.println(" + $SHADOW_ASYNC_TASK.optimizeAsyncTaskExecutor()V before @${if (it.opcode == Opcodes.ATHROW) "athrow" else "return"}: ${klass.name}.${method.name}${method.desc} ")

            }
        }
    }

}

private fun makeThreadName(name: String) = MARK + name

private val ClassNode.defaultClinit: MethodNode
    get() = MethodNode(Opcodes.ACC_STATIC, "<clinit>", "()V", null, null).apply {
        maxStack = 1
        instructions.add(InsnNode(Opcodes.RETURN))
    }


internal val MARK = "\u200B"

const val SHADOW = "com/didiglobal/booster/instrument/Shadow"
const val SHADOW_HANDLER_THREAD = "${SHADOW}HandlerThread"
const val SHADOW_THREAD = "${SHADOW}Thread"
const val SHADOW_TIMER = "${SHADOW}Timer"
const val SHADOW_EXECUTORS = "${SHADOW}Executors"
const val SHADOW_THREAD_POOL_EXECUTOR = "${SHADOW}ThreadPoolExecutor"
const val SHADOW_ASYNC_TASK = "${SHADOW}AsyncTask"

const val HANDLER_THREAD = "android/os/HandlerThread"
const val THREAD = "java/lang/Thread"
const val TIMER = "java/util/Timer"
const val EXECUTORS = "java/util/concurrent/Executors"
const val THREAD_POOL_EXECUTOR = "java/util/concurrent/ThreadPoolExecutor"
