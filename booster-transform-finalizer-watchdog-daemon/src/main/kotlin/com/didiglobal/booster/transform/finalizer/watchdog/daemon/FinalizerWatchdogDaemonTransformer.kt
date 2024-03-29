package com.didiglobal.booster.transform.finalizer.watchdog.daemon

import com.didiglobal.booster.kotlinx.touch
import com.didiglobal.booster.transform.ArtifactManager
import com.didiglobal.booster.transform.TransformContext
import com.didiglobal.booster.transform.asm.ClassTransformer
import com.didiglobal.booster.transform.asm.className
import com.didiglobal.booster.transform.asm.findAll
import com.didiglobal.booster.transform.finalizer.watchdog.daemon.Build.ARTIFACT
import com.didiglobal.booster.transform.util.ComponentHandler
import com.google.auto.service.AutoService
import org.objectweb.asm.Opcodes
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
 * Represents a class transformer for `FinalizerWatchdogDaemon` Thread
 * @author neighbWang
 */
@AutoService(ClassTransformer::class)
class FinalizerWatchdogDaemonTransformer : ClassTransformer {

    private lateinit var logger: PrintWriter
    private val applications = mutableSetOf<String>()

    override val name: String = ARTIFACT

    override fun onPreTransform(context: TransformContext) {
        val parser = SAXParserFactory.newInstance().newSAXParser()
        context.artifacts.get(ArtifactManager.MERGED_MANIFESTS).forEach { manifest ->
            val handler = ComponentHandler()
            parser.parse(manifest, handler)
            applications.addAll(handler.applications)
        }

        this.logger = getReport(context, "report.txt").touch().printWriter()
    }

    override fun onPostTransform(context: TransformContext) {
        this.logger.close()
    }

    override fun transform(context: TransformContext, klass: ClassNode): ClassNode {
        if (!this.applications.contains(klass.className)) {
            return klass
        }

        val method = klass.methods?.find {
            "${it.name}${it.desc}" == "onCreate()V"
        } ?: klass.defaultOnCreate

        method.instructions?.findAll(RETURN, ATHROW)?.forEach {
                method.instructions?.insertBefore(it, MethodInsnNode(INVOKESTATIC, FINALIZER_WATCHDOG_DAEMON_KILLER, "kill", "()V", false))
                logger.println(" + $FINALIZER_WATCHDOG_DAEMON_KILLER.kill()V: ${klass.name}.${method.name}${method.desc} ")
        }

        return klass
    }
}

private val ClassNode.defaultOnCreate: MethodNode
    get() = MethodNode(Opcodes.ACC_PUBLIC, "onCreate", "()V", null, null).apply {
        maxStack = 1
        instructions.add(InsnList().apply {
            add(VarInsnNode(ALOAD, 0))
            add(MethodInsnNode(INVOKESPECIAL, superName, name, desc, false))
            add(InsnNode(RETURN))
        })
    }

private const val FINALIZER_WATCHDOG_DAEMON_KILLER = "com/didiglobal/booster/instrument/FinalizerWatchdogDaemonKiller"




