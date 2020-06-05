package com.didiglobal.booster.transform.r.inline

import com.didiglobal.booster.kotlinx.Wildcard
import com.didiglobal.booster.kotlinx.asIterable
import com.didiglobal.booster.kotlinx.execute
import com.didiglobal.booster.kotlinx.file
import com.didiglobal.booster.kotlinx.ifNotEmpty
import com.didiglobal.booster.kotlinx.touch
import com.didiglobal.booster.transform.ArtifactManager.Companion.MERGED_RES
import com.didiglobal.booster.transform.ArtifactManager.Companion.SYMBOL_LIST
import com.didiglobal.booster.transform.TransformContext
import com.didiglobal.booster.transform.asm.ClassTransformer
import com.google.auto.service.AutoService
import org.gradle.api.logging.Logging
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.ClassWriter.COMPUTE_MAXS
import org.objectweb.asm.Opcodes.GETSTATIC
import org.objectweb.asm.tree.ClassNode
import org.objectweb.asm.tree.FieldInsnNode
import org.objectweb.asm.tree.LdcInsnNode
import java.io.File
import java.io.PrintWriter
import java.util.concurrent.ConcurrentHashMap
import java.util.regex.Pattern

internal const val R_STYLEABLE = "R\$styleable"
internal const val ANDROID_R = "android/R$"
internal const val COM_ANDROID_INTERNAL_R = "com/android/internal/R$"
internal const val R_REGEX = ".*/R\\\$.*|.*/R\\.*"

/**
 * Represents a class node transformer for constants shrinking
 *
 * @author johnsonlee
 */
@AutoService(ClassTransformer::class)
class RInlineTransformer : ClassTransformer {

    private lateinit var appPackage: String
    private lateinit var appRStyleable: String
    private lateinit var symbols: SymbolList
    private lateinit var ignores: Set<Wildcard>
    private lateinit var logger: PrintWriter
    private val removedR by lazy {
        ConcurrentHashMap<String, Int>()
    }

    override fun onPreTransform(context: TransformContext) {
        this.appPackage = context.originalApplicationId.replace('.', '/')
        this.logger = context.reportsDir.file(Build.ARTIFACT).file(context.name).file("report.txt").touch().printWriter()
        this.symbols = SymbolList.from(context.artifacts.get(SYMBOL_LIST).single())
        this.appRStyleable = "$appPackage/$R_STYLEABLE"
        this.ignores = context.getProperty(PROPERTY_IGNORES, "").trim().split(',').map(Wildcard.Companion::valueOf).toSet()

        if (this.symbols.isEmpty()) {
            logger_.error("Inline R symbols failed: R.txt doesn't exist or blank")
            this.logger.println("Inlining R symbols failed: R.txt doesn't exist or blank")
            return
        }

        val retainedSymbols: Set<String>
        val classpath = context.compileClasspath.map { it.absolutePath }
        if (classpath.any { it.contains(PREFIX_SUPPORT_CONSTRAINT_LAYOUT) || it.contains(PREFIX_JETPACK_CONSTRAINT_LAYOUT) }) {
            // Find symbols that should be retained
            retainedSymbols = context.findRetainedSymbols()
            if (retainedSymbols.isNotEmpty()) {
                this.ignores += setOf(Wildcard.valueOf("android/support/constraint/R\$id"))
                this.ignores += setOf(Wildcard.valueOf("androidx/constraintlayout/R\$id"))
            }
        } else {
            retainedSymbols = emptySet()
        }

        logger.println(classpath.joinToString("\n  - ", "classpath:\n  - ", "\n"))
        logger.println("$PROPERTY_IGNORES=$ignores\n")

        retainedSymbols.ifNotEmpty { symbols ->
            logger.println("Retained symbols:")
            symbols.forEach {
                logger.println("  - R.id.$it")
            }
            logger.println()
        }

    }

    override fun transform(context: TransformContext, klass: ClassNode): ClassNode {
        if (this.symbols.isEmpty()) {
            return klass
        }
        if (this.ignores.any { it.matches(klass.name) }) {
            logger.println("Ignore `${klass.name}`")
        } else if (Pattern.matches(R_REGEX, klass.name) && klass.name != appRStyleable) {
            klass.fields.clear()
            removedR[klass.name] = klass.bytes()
        } else {
            klass.replaceSymbolReferenceWithConstant()
        }

        return klass
    }

    private fun ClassNode.bytes() = ClassWriter(COMPUTE_MAXS).also {
        accept(it)
    }.toByteArray().size

    override fun onPostTransform(context: TransformContext) {
        val totalSize = removedR.map { it.value }.sum()
        val maxWidth = removedR.map { it.key.length }.max()?.plus(10) ?: 10
        this.logger.run {
            println("Delete files:")
            removedR.toSortedMap().forEach {
                println(" - `${it.key}`")
            }
            println("-".repeat(maxWidth))
            println("Total: $totalSize bytes")
            println()
            close()
        }
    }

    private fun ClassNode.replaceSymbolReferenceWithConstant() {
        methods.forEach { method ->
            val insns = method.instructions.iterator().asIterable().filter {
                it.opcode == GETSTATIC
            }.map {
                it as FieldInsnNode
            }.filter {
                ("I" == it.desc || "[I" == it.desc)
                        && it.owner.substring(it.owner.lastIndexOf('/') + 1).startsWith("R$")
                        && !(it.owner.startsWith(COM_ANDROID_INTERNAL_R) || it.owner.startsWith(ANDROID_R))
            }

            val intFields = insns.filter { "I" == it.desc }
            val intArrayFields = insns.filter { "[I" == it.desc }

            // Replace int field with constant
            intFields.forEach { field ->
                val type = field.owner.substring(field.owner.lastIndexOf("/R$") + 3)
                try {
                    method.instructions.insertBefore(field, LdcInsnNode(symbols.getInt(type, field.name)))
                    method.instructions.remove(field)
                    logger.println(" * ${field.owner}.${field.name} => ${symbols.getInt(type, field.name)}: $name.${method.name}${method.desc}")
                } catch (e: NullPointerException) {
                    logger.println(" ! Unresolvable symbol `${field.owner}.${field.name}`: $name.${method.name}${method.desc}")
                }
            }

            // Replace library's R fields with application's R fields
            intArrayFields.forEach { field ->
                field.owner = "$appPackage/${field.owner.substring(field.owner.lastIndexOf('/') + 1)}"
            }
        }
    }

}

/**
 * Find symbols that should be retained, such as:
 *
 * - attribute `constraint_referenced_ids` in `ConstraintLayout`
 */
private fun TransformContext.findRetainedSymbols(): Set<String> {
    return artifacts.get(MERGED_RES).map {
        RetainedSymbolCollector(it).execute()
    }.flatten().toSet()
}

private val PROPERTY_PREFIX = Build.ARTIFACT.replace('-', '.')

private val PROPERTY_IGNORES = "$PROPERTY_PREFIX.ignores"

private val PREFIX_SUPPORT_CONSTRAINT_LAYOUT = "${File.separatorChar}com.android.support.constraint${File.separatorChar}constraint-layout"

private val PREFIX_JETPACK_CONSTRAINT_LAYOUT = "${File.separator}androidx.constraintlayout${File.separator}constraintlayout"

private val logger_ = Logging.getLogger(RInlineTransformer::class.java)
