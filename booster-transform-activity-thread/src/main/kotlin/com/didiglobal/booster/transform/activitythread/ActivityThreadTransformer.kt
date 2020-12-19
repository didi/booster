package com.didiglobal.booster.transform.activitythread

import com.didiglobal.booster.kotlinx.touch
import com.didiglobal.booster.transform.ArtifactManager
import com.didiglobal.booster.transform.TransformContext
import com.didiglobal.booster.transform.activity.thread.Build
import com.didiglobal.booster.transform.asm.ClassTransformer
import com.didiglobal.booster.transform.asm.className
import com.didiglobal.booster.transform.asm.defaultOnCreate
import com.didiglobal.booster.transform.asm.findAll
import com.didiglobal.booster.transform.util.ComponentHandler
import com.google.auto.service.AutoService
import org.objectweb.asm.Opcodes.ATHROW
import org.objectweb.asm.Opcodes.INVOKESTATIC
import org.objectweb.asm.Opcodes.RETURN
import org.objectweb.asm.tree.ClassNode
import org.objectweb.asm.tree.LdcInsnNode
import org.objectweb.asm.tree.MethodInsnNode
import java.io.PrintWriter
import javax.xml.parsers.SAXParserFactory

/**
 * @author neighbWang
 */
@AutoService(ClassTransformer::class)
class ActivityThreadTransformer : ClassTransformer {

    private val applications = mutableSetOf<String>()

    private lateinit var logger: PrintWriter

    private lateinit var packagesIgnore: String

    override val name: String = Build.ARTIFACT

    override fun onPreTransform(context: TransformContext) {
        val parser = SAXParserFactory.newInstance().newSAXParser()
        context.artifacts.get(ArtifactManager.MERGED_MANIFESTS).forEach { manifest ->
            val handler = ComponentHandler()
            parser.parse(manifest, handler)
            applications.addAll(handler.applications)
        }

        this.logger = getReport(context, "report.txt").touch().printWriter()
        this.packagesIgnore = context.getProperty(PROPERTY_PACKAGES_IGNORE, "")
    }

    override fun onPostTransform(context: TransformContext) {
        this.logger.close()
    }

    override fun transform(context: TransformContext, klass: ClassNode): ClassNode {
        if (!this.applications.contains(klass.className)) {
            return klass
        }

        val onCreate = klass.methods.find {
            "${it.name}${it.desc}" == "onCreate()V"
        } ?: klass.defaultOnCreate.also {
            klass.methods.add(it)
        }

        onCreate.instructions?.findAll(RETURN, ATHROW)?.forEach {
            onCreate.instructions?.apply {
                insertBefore(it, LdcInsnNode(packagesIgnore.trim()))
                insertBefore(it, MethodInsnNode(INVOKESTATIC, ACTIVITY_THREAD_HOOKER, "hook", "(Ljava/lang/String;)V", false))
            }
            logger.println(" + $ACTIVITY_THREAD_HOOKER.hook()V: ${klass.name}.onCreate()V")
        }

        return klass
    }
}

private const val ACTIVITY_THREAD_HOOKER = "com/didiglobal/booster/instrument/ActivityThreadHooker"

private val PROPERTY_PACKAGES_IGNORE = Build.ARTIFACT.replace('-', '.') + ".packages.ignore"
