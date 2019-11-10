package com.didiglobal.booster.transform.res.check

import com.didiglobal.booster.kotlinx.asIterable
import com.didiglobal.booster.kotlinx.file
import com.didiglobal.booster.kotlinx.touch
import com.didiglobal.booster.transform.ArtifactManager
import com.didiglobal.booster.transform.TransformContext
import com.didiglobal.booster.transform.asm.ClassTransformer
import com.didiglobal.booster.transform.asm.className
import com.didiglobal.booster.util.ComponentHandler
import com.google.auto.service.AutoService
import org.objectweb.asm.Opcodes.ACC_PROTECTED
import org.objectweb.asm.Opcodes.ACC_PUBLIC
import org.objectweb.asm.Opcodes.ALOAD
import org.objectweb.asm.Opcodes.INVOKESPECIAL
import org.objectweb.asm.Opcodes.INVOKESTATIC
import org.objectweb.asm.Opcodes.RETURN
import org.objectweb.asm.tree.ClassNode
import org.objectweb.asm.tree.InsnList
import org.objectweb.asm.tree.InsnNode
import org.objectweb.asm.tree.MethodInsnNode
import org.objectweb.asm.tree.MethodNode
import org.objectweb.asm.tree.VarInsnNode
import java.io.PrintWriter
import javax.xml.parsers.SAXParserFactory


/**
 * @author neighbWang
 */
@AutoService(ClassTransformer::class)
class ResCheckTransformer : ClassTransformer {

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
        if (!this.applications.contains(klass.className)) {
            return klass
        }

        val attachBaseContext = klass.methods?.find {
            "${it.name}${it.desc}" == "attachBaseContext(Landroid/content/Context;)V"
        } ?: klass.defaultAttachBaseContext.also {
            klass.methods.add(it)
        }

        attachBaseContext.instructions?.apply {
            iterator().asIterable().find {
                it.opcode == INVOKESPECIAL
                        && context.klassPool.get(APPLICATION).isAssignableFrom((it as MethodInsnNode).owner)
                        && "${it.name}${it.desc}" == "attachBaseContext(Landroid/content/Context;)V"
            }?.let {
                insert(it, InsnList().apply {
                    add(VarInsnNode(ALOAD, 0))
                    add(MethodInsnNode(INVOKESTATIC, RES_CHECKER, "checkRes", "(Landroid/app/Application;)V", false))
                })
                logger.println(" + $RES_CHECKER.checkRes(Landroid/app/Application;)V: ${klass.name}.${(it as MethodInsnNode).name}${it.desc} ")
            }
        }

        val onCreate = klass.methods?.find {
            "${it.name}${it.desc}" == "onCreate()V"
        } ?: klass.defaultOnCreate.also {
            klass.methods.add(it)
        }


        onCreate.instructions?.apply {
            iterator().asIterable().find {
                it.opcode == INVOKESPECIAL && context.klassPool.get(APPLICATION).isAssignableFrom((it as MethodInsnNode).owner) && "${it.name}${it.desc}" == "onCreate()V"
            }.let {
                insert(it, InsnList().apply {
                    add(VarInsnNode(ALOAD, 0))
                    add(MethodInsnNode(INVOKESTATIC, RES_CHECKER, "checkRes", "(Landroid/app/Application;)V", false))
                })
                logger.println(" + $RES_CHECKER.checkRes(Landroid/app/Application;)V: ${klass.name}.${(it as MethodInsnNode).name}${it.desc} ")
            }
        }
        return klass
    }

}

private val ClassNode.defaultAttachBaseContext: MethodNode
    get() = MethodNode(ACC_PROTECTED, "attachBaseContext", "(Landroid/content/Context;)V", null, null).apply {
        maxStack = 1
        instructions.add(InsnList().apply {
            add(VarInsnNode(ALOAD, 0))
            add(VarInsnNode(ALOAD, 1))
            add(MethodInsnNode(INVOKESPECIAL, superName, name, desc, false))
            add(InsnNode(RETURN))
        })
    }

private val ClassNode.defaultOnCreate: MethodNode
    get() = MethodNode(ACC_PUBLIC, "onCreate", "()V", null, null).apply {
        maxStack = 1
        instructions.add(InsnList().apply {
            add(VarInsnNode(ALOAD, 0))
            add(MethodInsnNode(INVOKESPECIAL, superName, name, desc, false))
            add(InsnNode(RETURN))
        })
    }

const val APPLICATION = "android/app/Application"
const val RES_CHECKER = "com/didiglobal/booster/instrument/ResChecker"

