package com.didiglobal.booster.transform.webview

import com.didiglobal.booster.kotlinx.file
import com.didiglobal.booster.kotlinx.touch
import com.didiglobal.booster.transform.ArtifactManager
import com.didiglobal.booster.transform.TransformContext
import com.didiglobal.booster.transform.asm.ClassTransformer
import com.didiglobal.booster.transform.asm.className
import com.didiglobal.booster.transform.asm.findAll
import com.didiglobal.booster.transform.util.ComponentHandler
import com.google.auto.service.AutoService
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
 * Represents a class transformer for application optimization
 *
 * @author neighbWang
 */
@AutoService(ClassTransformer::class)
class WebViewTransformer : ClassTransformer {

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

        val method = klass.methods?.find {
            "${it.name}${it.desc}" == "onCreate()V"
        } ?: klass.defaultOnCreate.also {
            klass.methods.add(it)
        }

        method.instructions?.let { insn ->
            insn.findAll(RETURN, ATHROW).forEach { ret ->
                insn.insertBefore(ret, VarInsnNode(ALOAD, 0))
                insn.insertBefore(ret, MethodInsnNode(INVOKESTATIC, SHADOW_WEBVIEW, "preloadWebView", "(Landroid/app/Application;)V", false))
                logger.println(" + $SHADOW_WEBVIEW.preloadWebView(Landroid/app/Application;)V: ${klass.name}.${method.name}${method.desc} ")
            }
        }
        return klass
    }
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

private const val SHADOW_WEBVIEW = "com/didiglobal/booster/instrument/ShadowWebView"
