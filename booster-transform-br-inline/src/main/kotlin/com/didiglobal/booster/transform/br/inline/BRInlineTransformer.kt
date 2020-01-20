package com.didiglobal.booster.transform.br.inline

import com.didiglobal.booster.kotlinx.asIterable
import com.didiglobal.booster.kotlinx.file
import com.didiglobal.booster.kotlinx.touch
import com.didiglobal.booster.transform.ArtifactManager.Companion.ALL_CLASSES
import com.didiglobal.booster.transform.ArtifactManager.Companion.DATA_BINDING_DEPENDENCY_ARTIFACTS
import com.didiglobal.booster.transform.TransformContext
import com.didiglobal.booster.transform.asm.ClassTransformer
import com.didiglobal.booster.util.search
import com.google.auto.service.AutoService
import org.gradle.api.logging.Logging
import org.objectweb.asm.Opcodes.GETSTATIC
import org.objectweb.asm.tree.ClassNode
import org.objectweb.asm.tree.FieldInsnNode
import org.objectweb.asm.tree.LdcInsnNode
import java.io.File
import java.io.PrintWriter

internal const val BR_FILE_EXT = "-br.bin"

/**
 * Represents a class node transformer for constants shrinking
 *
 * @author linjiang
 */
@AutoService(ClassTransformer::class)
class BRInlineTransformer : ClassTransformer {

    private lateinit var symbols: SymbolList
    private lateinit var logger: PrintWriter
    private lateinit var validClasses: Set<String>

    override fun onPreTransform(context: TransformContext) {
        logger = context.reportsDir.file(Build.ARTIFACT).file(context.name).file("report.txt").touch().printWriter()
        validClasses = context.findValidClasses()
        val allBR = context.findAllBR()
        symbols = allBR.find { it.second == context.appBR }?.first?.let {
            SymbolList.from(it)
        } ?: SymbolList.Builder().build()
        if (symbols.isEmpty()) {
            "Inline BR symbols failed: BR.class doesn't exist or blank".apply {
                logger_.error(this)
                logger.println(this)
            }
            return
        }
        // Remove all BR class files
        allBR.also { pairs ->
            val totalSize = allBR.map { it.first.length() }.sum()
            val maxWidth = allBR.map { it.second.length }.max()?.plus(10) ?: 10

            logger.println("Delete files:")

            pairs.forEach {
                if (it.first.delete()) {
                    logger.println(" - `${it.second}`")
                }
            }

            logger.println("-".repeat(maxWidth))
            logger.println("Total: $totalSize bytes")
            logger.println()
        }
    }

    override fun transform(context: TransformContext, klass: ClassNode): ClassNode {
        if (symbols.isEmpty()) {
            return klass
        }
        klass.replaceSymbolReferenceWithConstant()
        return klass
    }

    override fun onPostTransform(context: TransformContext) {
        this.logger.close()
    }

    private val TransformContext.appBR
        get() = "${originalApplicationId.replace('.', '/')}/BR.class"

    private fun TransformContext.findValidClasses(): Set<String> {
        return if (isDataBindingEnabled) {
            artifacts.get(DATA_BINDING_DEPENDENCY_ARTIFACTS)
                    .filter { it.name.endsWith(BR_FILE_EXT) }
                    .map { "${it.name.substringBefore("-").replace('.', '/')}/BR.class" }
                    .plus(appBR)
                    .toSet()
        } else {
            emptySet()
        }
    }

    private fun TransformContext.findAllBR(): List<Pair<File, String>> {
        return artifacts.get(ALL_CLASSES).map { classes ->
            val base = classes.toURI()
            classes.search { r ->
                base.relativize(r.toURI()).path in validClasses
            }.map { r ->
                r to base.relativize(r.toURI()).path
            }
        }.flatten()
    }

    private fun ClassNode.replaceSymbolReferenceWithConstant() {
        methods.forEach { method ->
            method.instructions.iterator().asIterable().filter {
                it.opcode == GETSTATIC
            }.map {
                it as FieldInsnNode
            }.filter {
                "I" == it.desc && "${it.owner}.class" in validClasses
            }.forEach { field ->
                // Replace int field with constant
                try {
                    method.instructions.insertBefore(field, LdcInsnNode(symbols.getInt(field.name)))
                    method.instructions.remove(field)
                    logger.println(" * ${field.owner}.${field.name} => ${symbols.getInt(field.name)}: ${name}.${method.name}${method.desc}")
                } catch (e: NullPointerException) {
                    logger.println(" ! Unresolvable symbol `${field.owner}.${field.name}` : $name.${method.name}${method.desc}")
                }
            }
        }
    }

}


private val logger_ = Logging.getLogger(BRInlineTransformer::class.java)
