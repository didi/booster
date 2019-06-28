package com.didiglobal.booster.transform.activitythread

import com.didiglobal.booster.kotlinx.file
import com.didiglobal.booster.kotlinx.touch
import com.didiglobal.booster.transform.ArtifactManager
import com.didiglobal.booster.transform.TransformContext
import com.didiglobal.booster.transform.activity.thread.Build
import com.didiglobal.booster.transform.asm.ClassTransformer
import com.didiglobal.booster.transform.asm.className
import com.didiglobal.booster.transform.asm.findAll
import com.didiglobal.booster.util.ComponentHandler
import com.google.auto.service.AutoService
import org.objectweb.asm.Opcodes
import org.objectweb.asm.Opcodes.ACC_PUBLIC
import org.objectweb.asm.Opcodes.ALOAD
import org.objectweb.asm.Opcodes.ATHROW
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
class ActivityThreadTransformer : ClassTransformer {

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

        mapOf(
            "<clinit>()V" to klass.defaultClinit,
            "<init>()V" to klass.defaultInit,
            "onCreate()V" to klass.defaultOnCreate
        ).forEach { (unique, defaultMethod) ->
            val method = klass.methods?.find {
                "${it.name}${it.desc}" == unique
            } ?: defaultMethod.also {
                klass.methods.add(it)
            }
            method.instructions?.findAll(RETURN, ATHROW)?.forEach {
                method.instructions?.insertBefore(it, MethodInsnNode(INVOKESTATIC, ACTIVITY_THREAD_HOOKER, "hook", "()V", false))
                logger.println(" + $ACTIVITY_THREAD_HOOKER.hook()V before @${if (it.opcode == ATHROW) "athrow" else "return"}: ${klass.name}.${method.name}${method.desc}")
            }
        }

        return klass
    }
}

private val ClassNode.defaultClinit: MethodNode
    get() = MethodNode(Opcodes.ACC_STATIC, "<clinit>", "()V", null, null).apply {
        maxStack = 1
        instructions.add(InsnNode(RETURN))
    }

private val ClassNode.defaultInit: MethodNode
    get() = MethodNode(ACC_PUBLIC, "<init>", "()V", null, null).apply {
        maxStack = 1
        instructions.add(InsnList().apply {
            add(VarInsnNode(ALOAD, 0))
            add(MethodInsnNode(INVOKESPECIAL, superName, name, desc, false))
            add(InsnNode(RETURN))
        })
    }

private val ClassNode.defaultOnCreate: MethodNode
    get() = MethodNode(ACC_PUBLIC, "onCreate", "()V", null, null).apply {
        instructions.add(InsnList().apply {
            add(VarInsnNode(ALOAD, 0))
            add(MethodInsnNode(INVOKESPECIAL, superName, name, desc, false))
            add(InsnNode(RETURN))
        })
        maxStack = 1
    }

const val ACTIVITY_THREAD_HOOKER = "com/didiglobal/booster/instrument/ActivityThreadHooker"
