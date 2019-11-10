package com.didiglobal.booster.transform.sharedpreferences

import com.didiglobal.booster.kotlinx.asIterable
import com.didiglobal.booster.kotlinx.file
import com.didiglobal.booster.kotlinx.touch
import com.didiglobal.booster.transform.ArtifactManager
import com.didiglobal.booster.transform.TransformContext
import com.didiglobal.booster.transform.asm.ClassTransformer
import com.didiglobal.booster.transform.asm.className
import com.didiglobal.booster.transform.shared.preferences.Build
import com.didiglobal.booster.util.ComponentHandler
import com.google.auto.service.AutoService
import org.objectweb.asm.Opcodes
import org.objectweb.asm.Opcodes.INVOKESTATIC
import org.objectweb.asm.Opcodes.INVOKEVIRTUAL
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
 * Represents a transformer for *SharedPreferences* optimization
 *
 * @author neighbWang
 */
@AutoService(ClassTransformer::class)
class SharedPreferencesTransformer : ClassTransformer {

    private lateinit var logger: PrintWriter
    private val applications = mutableSetOf<String>()

    override fun onPreTransform(context: TransformContext) {
        val parser = SAXParserFactory.newInstance().newSAXParser()
        context.artifacts.get(ArtifactManager.MERGED_MANIFESTS).forEach { manifest ->
            val handler = ComponentHandler()
            parser.parse(manifest, handler)
            applications.addAll(handler.applications)
        }
        logger = context.reportsDir.file(Build.ARTIFACT).file(context.name).file("report.txt").touch().printWriter()
    }

    override fun onPostTransform(context: TransformContext) {
        logger.close()
    }

    override fun transform(context: TransformContext, klass: ClassNode): ClassNode {
        if (klass.name.startsWith(BOOSTER_DIRECTORY_PREFIX) || klass.name.startsWith(SUPPORT_MULTIDEX_PACKAGE_PREFIX)) {
            return klass
        }

        if (this.applications.contains(klass.className)) {
            initSharedPreferences(context, klass)
        }

        klass.methods.forEach { method ->
            method.instructions.iterator().asIterable().filter {
                it.opcode == INVOKEVIRTUAL
            }.map {
                it as MethodInsnNode
            }.forEach {
                mapOf(
                    CONTEXT_GET_SHARED_PREFERENCES to SHADOW_CONTEXT_GET_SHARED_PREFERENCES,
                    ACTIVITY_GET_PREFERENCES to SHADOW_ACTIVITY_GET_PREFERENCES
                ).forEach { (original, shadow) ->
                    if (it.opcode == original.opcode && it.name == original.name && it.desc == original.desc && context.klassPool.get(original.owner).isAssignableFrom(it.owner)) {
                        logger.println(" * ${shadow.owner}${shadow.name}${shadow.desc} => ${it.owner}.${it.name}${it.desc}: ${klass.name}.${method.name}${method.desc}")
                        it.opcode = shadow.opcode
                        it.owner = shadow.owner
                        it.name = shadow.name
                        it.desc = shadow.desc
                    }
                }
            }
        }
        return klass
    }

    private fun initSharedPreferences(context: TransformContext, klass: ClassNode) {
        val attachBaseContext = klass.methods?.find {
            "${it.name}${it.desc}" == "attachBaseContext(Landroid/content/Context;)V"
        } ?: klass.defaultAttachBaseContextMethod.also {
            klass.methods.add(it)
        }

        attachBaseContext.instructions?.apply {
            iterator().asIterable().find {
                it.opcode == Opcodes.INVOKESPECIAL
                        && context.klassPool.get(APPLICATION).isAssignableFrom((it as MethodInsnNode).owner)
                        && "${it.name}${it.desc}" == "attachBaseContext(Landroid/content/Context;)V"
            }?.let {
                insert(it, InsnList().apply {
                    add(VarInsnNode(Opcodes.ALOAD, 0))
                    add(MethodInsnNode(INVOKESTATIC, BOOSTER_SHARED_PREFERENCES, "init", "(L$CONTEXT;)V", false))
                })
                logger.println(" + $BOOSTER_SHARED_PREFERENCES.init(L$CONTEXT;)V: ${klass.name}.${(it as MethodInsnNode).name}${it.desc} ")
            }
        }

    }
}

private const val ACTIVITY = "android/app/Activity"
private const val CONTEXT = "android/content/Context"
private const val APPLICATION = "android/app/Application"
private const val SHARED_PREFERENCES = "android/content/SharedPreferences"
private const val SUPPORT_MULTIDEX_PACKAGE_PREFIX = "android/support/multidex/"
private const val BOOSTER_DIRECTORY_PREFIX = "com/didiglobal/booster/instrument"
private const val SHADOW_SHARED_PREFERENCES = "$BOOSTER_DIRECTORY_PREFIX/ShadowSharedPreferences"
private const val BOOSTER_SHARED_PREFERENCES = "$BOOSTER_DIRECTORY_PREFIX/sharedpreferences/BoosterSharedPreferences"

private val CONTEXT_GET_SHARED_PREFERENCES = MethodInsnNode(INVOKEVIRTUAL, CONTEXT, "getSharedPreferences", "(Ljava/lang/String;I)L$SHARED_PREFERENCES;")
private val SHADOW_CONTEXT_GET_SHARED_PREFERENCES = MethodInsnNode(INVOKESTATIC, SHADOW_SHARED_PREFERENCES, "getSharedPreferences", "(L$CONTEXT;Ljava/lang/String;I)L$SHARED_PREFERENCES;")
private val ACTIVITY_GET_PREFERENCES = MethodInsnNode(INVOKEVIRTUAL, ACTIVITY, "getPreferences", "(I)L$SHARED_PREFERENCES;")
private val SHADOW_ACTIVITY_GET_PREFERENCES = MethodInsnNode(INVOKESTATIC, SHADOW_SHARED_PREFERENCES, "getPreferences", "(L$ACTIVITY;I)L$SHARED_PREFERENCES;")

private val ClassNode.defaultAttachBaseContextMethod: MethodNode
    get() = MethodNode(Opcodes.ACC_PROTECTED, "attachBaseContext", "(Landroid/content/Context;)V", null, null).apply {
        maxStack = 1
        instructions.add(InsnList().apply {
            add(VarInsnNode(Opcodes.ALOAD, 0))
            add(VarInsnNode(Opcodes.ALOAD, 1))
            add(MethodInsnNode(Opcodes.INVOKESPECIAL, superName, name, desc, false))
            add(InsnNode(RETURN))
        })
    }