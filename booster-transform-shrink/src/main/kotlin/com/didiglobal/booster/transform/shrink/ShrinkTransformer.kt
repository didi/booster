package com.didiglobal.booster.transform.shrink

import com.didiglobal.booster.kotlinx.Wildcard
import com.didiglobal.booster.kotlinx.asIterable
import com.didiglobal.booster.kotlinx.execute
import com.didiglobal.booster.kotlinx.file
import com.didiglobal.booster.kotlinx.ifNotEmpty
import com.didiglobal.booster.kotlinx.touch
import com.didiglobal.booster.transform.ArtifactManager.Companion.JAVAC
import com.didiglobal.booster.transform.ArtifactManager.Companion.MERGED_RES
import com.didiglobal.booster.transform.ArtifactManager.Companion.SYMBOL_LIST
import com.didiglobal.booster.transform.TransformContext
import com.didiglobal.booster.transform.asm.ClassTransformer
import com.didiglobal.booster.util.search
import com.google.auto.service.AutoService
import org.objectweb.asm.Opcodes.ACC_FINAL
import org.objectweb.asm.Opcodes.ACC_STATIC
import org.objectweb.asm.Opcodes.GETSTATIC
import org.objectweb.asm.tree.ClassNode
import org.objectweb.asm.tree.FieldInsnNode
import org.objectweb.asm.tree.FieldNode
import org.objectweb.asm.tree.LdcInsnNode
import java.io.File
import java.io.PrintWriter

internal const val R_STYLEABLE = "R\$styleable"
internal const val ANDROID_R = "android/R$"
internal const val COM_ANDROID_INTERNAL_R = "com/android/internal/R$"

/**
 * Represents a class node transformer for constants shrinking
 *
 * @author johnsonlee
 */
@AutoService(ClassTransformer::class)
class ShrinkTransformer : ClassTransformer {

    private lateinit var appPackage: String
    private lateinit var appRStyleable: String
    private lateinit var symbols: SymbolList
    private lateinit var ignores: Set<Wildcard>
    private lateinit var logger: PrintWriter

    override fun onPreTransform(context: TransformContext) {
        this.appPackage = context.originalApplicationId.replace('.', '/')
        this.logger = context.reportsDir.file(Build.ARTIFACT).file(context.name).file("report.txt").touch().printWriter()
        this.symbols = SymbolList.from(context.artifacts.get(SYMBOL_LIST).single())
        this.appRStyleable = "$appPackage/$R_STYLEABLE"
        this.ignores = context.getProperty(PROPERTY_IGNORES)?.split(',')?.map { Wildcard(it) }?.toSet() ?: emptySet()

        val retainedSymbols: Set<String>
        val classpath = context.compileClasspath.map { it.absolutePath }
        if (classpath.any { it.contains(PREFIX_CONSTRAINT_LAYOUT) }) {
            // Find symbols that should be retained
            retainedSymbols = context.findRetainedSymbols()
            if (retainedSymbols.isNotEmpty()) {
                this.ignores += setOf(Wildcard.valueOf("android/support/constraint/R\$id"))
            }
        } else {
            retainedSymbols = emptySet()
        }

        if (classpath.any { it.contains(PREFIX_GREENDAO) }) {
            this.ignores += Wildcard.valueOf(PATTERN_FIELD_TABLENAME)
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

        // Remove redundant R class files
        val redundant = context.findRedundantR()
        redundant.ifNotEmpty { pairs ->
            val totalSize = redundant.map { it.first.length() }.sum()
            val maxWidth = redundant.map { it.second.length }.max()?.plus(10) ?: 10

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
        if (this.ignores.any { it.matches(klass.name) }) {
            logger.println("Ignore `${klass.name}`")
        } else {
            klass.removeConstantFields()
            klass.replaceSymbolReferenceWithConstant()
        }
        return klass
    }

    override fun onPostTransform(context: TransformContext) {
        this.logger.close()
    }

    private fun TransformContext.findRedundantR(): List<Pair<File, String>> {
        return artifacts.get(JAVAC).map { classes ->
            val base = classes.toURI()

            classes.search { r ->
                r.name.startsWith("R") && r.name.endsWith(".class") && (r.name[1] == '$' || r.name.length == 7)
            }.map { r ->
                r to base.relativize(r.toURI()).path.substringBeforeLast(".class")
            }
        }.flatten().filter {
            it.second != appRStyleable // keep application's R$styleable.class
        }.filter { pair ->
            !ignores.any { it.matches(pair.second) }
        }
    }

    private fun ClassNode.removeConstantFields() {
        fields.map {
            it as FieldNode
        }.filter { field ->
            !ignores.any { it.matches("$name.${field.name}:${field.desc}") }
        }.filter {
            0 != (ACC_STATIC and it.access) && 0 != (ACC_FINAL and it.access) && it.value != null
        }.forEach {
            fields.remove(it)
            logger.println("Remove `$name.${it.name} : ${it.desc}` = ${it.valueAsString()}")
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
                } catch (e: NullPointerException) {
                    logger.println("Unresolvable symbol `R.$type.${field.name}` : $name.${method.name}${method.desc}")
                }
            }

            // Replace library's R fields with application's R fields
            intArrayFields.forEach { field ->
                field.owner = "$appPackage/${field.owner.substring(field.owner.lastIndexOf('/') + 1)}"
            }
        }
    }

}

private fun FieldNode.valueAsString() = when {
    value is String -> "\"$value\""
    else -> value.toString()
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

private val PREFIX_CONSTRAINT_LAYOUT = "${File.separatorChar}com.android.support.constraint${File.separatorChar}constraint-layout"

private val PREFIX_GREENDAO = "${File.separatorChar}org.greenrobot${File.separatorChar}greendao${File.separatorChar}"

internal const val PATTERN_FIELD_TABLENAME = "*.TABLENAME:Ljava/lang/String;"
