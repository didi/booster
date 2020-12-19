package com.didiglobal.booster.transform.logcat

import com.didiglobal.booster.kotlinx.asIterable
import com.didiglobal.booster.kotlinx.touch
import com.didiglobal.booster.transform.TransformContext
import com.didiglobal.booster.transform.asm.ClassTransformer
import com.google.auto.service.AutoService
import org.objectweb.asm.Opcodes.GETSTATIC
import org.objectweb.asm.Opcodes.INVOKESTATIC
import org.objectweb.asm.Opcodes.INVOKEVIRTUAL
import org.objectweb.asm.tree.ClassNode
import org.objectweb.asm.tree.FieldInsnNode
import org.objectweb.asm.tree.MethodInsnNode
import java.io.PrintWriter

private const val LOGCAT = "android/util/Log"
private const val THROWABLE = "java/lang/Throwable"
private const val SYSTEM = "java/lang/System"
private const val INSTRUMENT = "com/didiglobal/booster/instrument/"
private const val SHADOW_LOG = "${INSTRUMENT}ShadowLog"
private const val SHADOW_SYSTEM = "${INSTRUMENT}ShadowSystem"
private const val SHADOW_THROWABLE = "${INSTRUMENT}ShadowThrowable"
private val SHADOW_LOG_METHODS = setOf("v", "d", "i", "w", "e", "wtf", "println")

/**
 * @author neighbWang
 */
@AutoService(ClassTransformer::class)
class LogcatTransformer : ClassTransformer {

    private lateinit var logger: PrintWriter

    override val name: String = Build.ARTIFACT

    override fun onPreTransform(context: TransformContext) {
        this.logger = getReport(context, "report.txt").touch().printWriter()
    }

    override fun onPostTransform(context: TransformContext) {
        this.logger.close()
    }

    override fun transform(context: TransformContext, klass: ClassNode): ClassNode {
        if (context.isDebuggable || klass.name.startsWith(INSTRUMENT)) {
            return klass
        }
        klass.methods.forEach { method ->
            method.instructions?.iterator()?.asIterable()?.filter {
                when (it.opcode) {
                    INVOKESTATIC -> (it as MethodInsnNode).owner == LOGCAT && SHADOW_LOG_METHODS.contains(it.name)
                    INVOKEVIRTUAL -> (it as MethodInsnNode).name == "printStackTrace" && it.desc == "()V" && context.klassPool.get(THROWABLE).isAssignableFrom(it.owner)
                    GETSTATIC -> (it as FieldInsnNode).owner == SYSTEM && (it.name == "out" || it.name == "err")
                    else -> false
                }
            }?.forEach {
                when (it.opcode) {
                    INVOKESTATIC -> {
                        logger.println(" * ${(it as MethodInsnNode).owner}.${it.name}${it.desc} => $SHADOW_LOG.${it.name}${it.desc}: ${klass.name}.${method.name}${method.desc}")
                        it.owner = SHADOW_LOG
                    }
                    INVOKEVIRTUAL -> {
                        logger.println(" * ${(it as MethodInsnNode).owner}.${it.name}${it.desc} => $SHADOW_LOG.${it.name}${it.desc}: ${klass.name}.${method.name}${method.desc}")
                        it.apply {
                            itf = false
                            owner = SHADOW_THROWABLE
                            desc = "(Ljava/lang/Throwable;)V"
                            opcode = INVOKESTATIC
                        }
                    }
                    GETSTATIC -> {
                        logger.println(" * ${(it as FieldInsnNode).owner}.${it.name}${it.desc} => $SHADOW_LOG.${it.name}${it.desc}: ${klass.name}.${method.name}${method.desc}")
                        it.owner = SHADOW_SYSTEM
                    }
                }
            }
        }
        return klass
    }

}


