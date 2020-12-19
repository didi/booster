package com.didiglobal.booster.transform.mediaplayer

import com.didiglobal.booster.kotlinx.asIterable
import com.didiglobal.booster.kotlinx.touch
import com.didiglobal.booster.transform.TransformContext
import com.didiglobal.booster.transform.asm.ClassTransformer
import com.didiglobal.booster.transform.asm.transform
import com.didiglobal.booster.transform.media.player.Build
import com.google.auto.service.AutoService
import org.objectweb.asm.Opcodes
import org.objectweb.asm.tree.ClassNode
import org.objectweb.asm.tree.MethodInsnNode
import org.objectweb.asm.tree.TypeInsnNode
import java.io.PrintWriter

private const val MEDIA_PLAYER = "android/media/MediaPlayer"

private const val SHADOW_MEDIA_PLAYER = "com/didiglobal/booster/instrument/ShadowMediaPlayer"

/**
 * @author neighbWang
 */
@AutoService(ClassTransformer::class)
class MediaPlayerTransformer : ClassTransformer {

    private lateinit var logger: PrintWriter

    override val name: String = Build.ARTIFACT

    override fun onPreTransform(context: TransformContext) {
        this.logger = getReport(context, "report.txt").touch().printWriter()
    }

    override fun onPostTransform(context: TransformContext) {
        this.logger.close()
    }

    override fun transform(context: TransformContext, klass: ClassNode): ClassNode {
        if (klass.name == SHADOW_MEDIA_PLAYER) {
            return klass
        }
        klass.methods?.forEach { method ->
            method.instructions?.iterator()?.asIterable()?.filter {
                when (it.opcode) {
                    Opcodes.INVOKESTATIC -> (it as MethodInsnNode).owner == MEDIA_PLAYER && it.name == "create"
                    Opcodes.NEW -> (it as TypeInsnNode).desc == MEDIA_PLAYER
                    else -> false
                }
            }?.forEach {
                if (it.opcode == Opcodes.INVOKESTATIC) {
                    logger.println(" * ${(it as MethodInsnNode).owner}.${it.name}${it.desc} => $SHADOW_MEDIA_PLAYER.${it.name}${it.desc}: ${klass.name}.${method.name}${method.desc}")
                    it.owner = SHADOW_MEDIA_PLAYER
                } else if (it.opcode == Opcodes.NEW) {
                    (it as TypeInsnNode).transform(klass, method, it, SHADOW_MEDIA_PLAYER)
                    logger.println(" * new ${it.desc}() => $SHADOW_MEDIA_PLAYER.newMediaPlayer:()L$MEDIA_PLAYER: ${klass.name}.${method.name}${method.desc}")
                }
            }
        }
        return klass
    }

}

