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

        val clinit = klass.methods?.find {
            "${it.name}${it.desc}" == "<clinit>()V"
        } ?: klass.defaultClinit

        clinit.instructions?.findAll(RETURN, ATHROW)?.forEach {
            clinit.instructions?.insertBefore(it, MethodInsnNode(INVOKESTATIC, ACTIVITY_THREAD_HOOKER, "hook", "()V", false))
            logger.println(" + $ACTIVITY_THREAD_HOOKER.hook()V before @${if (it.opcode == ATHROW) "athrow" else "return"}: ${klass.name}.${clinit.name}${clinit.desc}")
        }


        val init = klass.methods?.find {
            "${it.name}${it.desc}" == "<init>()V"
        } ?: klass.defaultInit

        init.instructions?.findAll(RETURN, ATHROW)?.forEach {
            init.instructions?.insertBefore(it, MethodInsnNode(INVOKESTATIC, ACTIVITY_THREAD_HOOKER, "hook", "()V", false))
            logger.println(" + $ACTIVITY_THREAD_HOOKER.hook()V before @${if (it.opcode == ATHROW) "athrow" else "return"}: ${klass.name}.${init.name}${init.desc}")
        }

        val onCreate = klass.methods?.find {
            "${it.name}${it.desc}" == "onCreate()V"
        } ?: klass.defaultOnCreate

        onCreate.instructions?.findAll(RETURN, ATHROW)?.forEach {
            onCreate.instructions?.insertBefore(it, MethodInsnNode(INVOKESTATIC, ACTIVITY_THREAD_HOOKER, "hook", "()V", false))
            logger.println(" + $ACTIVITY_THREAD_HOOKER.hook()V before @${if (it.opcode == ATHROW) "athrow" else "return"}: ${klass.name}.${onCreate.name}${onCreate.desc}")
        }
        return klass
    }
}

private val ClassNode.defaultClinit: MethodNode
    get() = MethodNode(Opcodes.ACC_STATIC, "<clinit>", "()V", null, null).apply {
        maxStack = 1
        instructions.insert(InsnNode(RETURN))
        methods?.add(this)
    }

private val ClassNode.defaultInit: MethodNode
    get() = MethodNode(ACC_PUBLIC, "<init>", "()V", null, null).apply {
        maxStack = 1
        instructions.insert(InsnList().apply {
            add(VarInsnNode(ALOAD, 0))
            add(MethodInsnNode(INVOKESPECIAL, superName, name, desc, false))
            add(InsnNode(RETURN))
        })
        methods?.add(this)
    }


private val ClassNode.defaultOnCreate: MethodNode
    get() = MethodNode(ACC_PUBLIC, "onCreate", "()V", null, null).apply {
        instructions?.add(InsnList().apply {
            add(VarInsnNode(ALOAD, 0))
            add(MethodInsnNode(INVOKESPECIAL, superName, name, desc, false))
            add(InsnNode(RETURN))
        })
        maxStack = 1
        methods?.add(this)
    }

const val ACTIVITY_THREAD_HOOKER = "com/didiglobal/booster/instrument/ActivityThreadHooker"