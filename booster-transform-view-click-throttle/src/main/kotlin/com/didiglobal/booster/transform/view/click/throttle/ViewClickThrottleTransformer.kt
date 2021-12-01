package com.didiglobal.booster.transform.view.click.throttle

import com.didiglobal.booster.kotlinx.Wildcard
import com.didiglobal.booster.kotlinx.touch
import com.didiglobal.booster.transform.TransformContext
import com.didiglobal.booster.transform.asm.ClassTransformer
import com.didiglobal.booster.transform.asm.findAll
import com.didiglobal.booster.transform.asm.getValue
import com.didiglobal.booster.transform.asm.isStatic
import com.google.auto.service.AutoService
import org.objectweb.asm.Handle
import org.objectweb.asm.Opcodes
import org.objectweb.asm.tree.*
import java.io.PrintWriter
import java.util.concurrent.CopyOnWriteArraySet
import java.util.regex.Pattern

/**
 * provide throttling for View's onClick events.
 * take the first click event within duration.
 */
@AutoService(ClassTransformer::class)
class ViewClickThrottleTransformer : ClassTransformer {
    override val name: String = Build.ARTIFACT

    private lateinit var logger: PrintWriter
    private var globalDuration = DEFAULT_DURATION
    private var global = DEFAULT_GLOBAL
    private lateinit var ignores: Set<Wildcard>
    private lateinit var includes: Set<Wildcard>

    override fun onPreTransform(context: TransformContext) {
        this.logger = getReport(context, "report.txt").touch().printWriter()
        this.globalDuration = context.getProperty(PROPERTY_DURATION, DEFAULT_DURATION)
        this.global = context.getProperty(PROPERTY_GLOBAL, DEFAULT_GLOBAL)
        this.ignores = context.getProperty(PROPERTY_IGNORES, "").trim().split(',')
                .filter(String::isNotEmpty)
                .map(Wildcard.Companion::valueOf).toSet()
        this.includes = context.getProperty(PROPERTY_INCLUDES, "").trim().split(',')
                .filter(String::isNotEmpty)
                .map(Wildcard.Companion::valueOf).toSet()
        logger.println("$PROPERTY_IGNORES=$ignores")
        logger.println("$PROPERTY_INCLUDES=$includes")
        logger.println("$PROPERTY_DURATION=$globalDuration")
        logger.println("$PROPERTY_GLOBAL=$global\n")
    }

    override fun onPostTransform(context: TransformContext) {
        this.logger.close()
    }

    override fun transform(context: TransformContext, klass: ClassNode): ClassNode {
        if (Pattern.matches(R_REGEX, klass.name)) return klass
        if (ignores.isNotEmpty()) {
            if (this.ignores.any { it.matches(klass.name) }) logger.println("Ignore `${klass.name}`") else klass.handle()
            return klass
        }
        if (includes.isNotEmpty()) {
            if (this.includes.any { it.matches(klass.name) }) {
                logger.println("Include `${klass.name}`")
                klass.handle()
            }
            return klass
        }
        klass.handle()
        return klass
    }

    private fun ClassNode.handle() {
        val lambdaFuns = CopyOnWriteArraySet<String>()
        // find onclickListener lambda
        methods?.forEach { method ->
            method.instructions.findAll(Opcodes.INVOKEDYNAMIC).filter { n ->
                val node = n as InvokeDynamicInsnNode
                node.name == ON_CLICK_FUN && node.desc.endsWith("Landroid/view/View\$OnClickListener;")
            }.forEach { clickn ->
                val clickNode = clickn as InvokeDynamicInsnNode
                clickNode.bsmArgs.forEach { bsmArg ->
                    if (bsmArg is Handle && bsmArg.desc.endsWith(VIEW_DESC_END)) {
                        lambdaFuns.add("${bsmArg.owner}${bsmArg.name}${bsmArg.desc}")
                    }
                }
            }
        }
        // android.view.View.OnClickListener
        if (interfaces?.find { it == VIEW_ON_CLICK_LISTENER } != null) {
            methods?.find { it.name == ON_CLICK_FUN && it.desc == VIEW_DESC }?.insertThrottle(this)
        }
        // lambda
        methods?.forEach {
            if (lambdaFuns.contains("${this.name}${it.name}${it.desc}")) {
                it.insertThrottle(this)
            }
        }
    }

    /**
     *
     * void onClick(View view) {
     *      // insert
     *      ThrottleUtil.setGlobal(global);
     *      ThrottleUtil.setGlobalDuration(globalDuration);
     *      if (duration > 0){
     *          ThrottleUtil.setDuration(duration);
     *      }
     *      if(view != null && !ThrottleUtil.check(view){
     *          return;
     *      }
     *
     *      // origin code
     *      // ...
     *
     * }
     *
     */
    private fun MethodNode.insertThrottle(klass: ClassNode) {
        val throttle = InsnList()
        // ThrottleUtil.setGlobal(global)
        throttle.add(InsnNode(if (global) Opcodes.ICONST_1 else Opcodes.ICONST_0))
        throttle.add(MethodInsnNode(Opcodes.INVOKESTATIC, THROTTLE_UTIL, "setGlobal", "(Z)V"))
        if (globalDuration >= 0) {
            // ThrottleUtil.setGlobalDuration(globalDuration)
            throttle.add(LdcInsnNode(globalDuration))
            throttle.add(MethodInsnNode(Opcodes.INVOKESTATIC, THROTTLE_UTIL, "setGlobalDuration", "(J)V"))
        }
        var duration = -1L
        invisibleAnnotations?.find { it.desc == THROTTLE_ANNOTATION_DESC }?.let {
            duration = it.getValue("duration") ?: -1L
        }
        if (duration >= 0) {
            // invoke ThrottleUtil.setDuration(duration)
            throttle.add(LdcInsnNode(duration))
            throttle.add(MethodInsnNode(Opcodes.INVOKESTATIC, THROTTLE_UTIL, "setDuration", "(J)V"))
        }
        val parameterSize = desc.split(';').size - 1
        var viewIndex = if (desc == VIEW_DESC) 1 else parameterSize
        if (isStatic) viewIndex--
        throttle.apply {
            // invoke ThrottleUtil.check(view)
            add(VarInsnNode(Opcodes.ALOAD, viewIndex))
            val label = LabelNode()
            add(JumpInsnNode(Opcodes.IFNULL, label))
            add(VarInsnNode(Opcodes.ALOAD, viewIndex))
            add(MethodInsnNode(Opcodes.INVOKESTATIC, THROTTLE_UTIL, "check", "(Landroid/view/View;)Z"))
            add(JumpInsnNode(Opcodes.IFNE, label))
            add(InsnNode(Opcodes.RETURN))
            add(label)
            // fix warning: no destination frame
            add(FrameNode(Opcodes.F_SAME, 0, null, 0, null))
        }
        instructions.first ?: return
        instructions.insertBefore(instructions.first, throttle)
        logger.println("${klass.name}#${if (isStatic) "static" else ""} $name$desc viewIndex:$viewIndex")
    }
}

private const val THROTTLE_ANNOTATION_DESC = "Lcom/didiglobal/booster/instrument/view/click/throttle/Throttle;"
private const val VIEW_ON_CLICK_LISTENER = "android/view/View\$OnClickListener"
private const val ON_CLICK_FUN = "onClick"
private const val VIEW_DESC = "(Landroid/view/View;)V"
private const val VIEW_DESC_END = "Landroid/view/View;)V"
private const val THROTTLE_UTIL = "com/didiglobal/booster/instrument/view/click/throttle/ThrottleUtil"
private const val R_REGEX = ".*/R\\\$.*|.*/R\\.*"
