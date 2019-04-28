package com.didiglobal.booster.transform.shrink

import com.didiglobal.booster.kotlinx.BLUE
import com.didiglobal.booster.kotlinx.RED
import com.didiglobal.booster.kotlinx.RESET
import com.didiglobal.booster.kotlinx.YELLOW
import com.didiglobal.booster.kotlinx.asIterable
import com.didiglobal.booster.kotlinx.head
import com.didiglobal.booster.kotlinx.separatorsToSystem
import com.didiglobal.booster.transform.ArtifactManager.Companion.JAVAC
import com.didiglobal.booster.transform.ArtifactManager.Companion.MERGED_RES
import com.didiglobal.booster.transform.ArtifactManager.Companion.SYMBOL_LIST
import com.didiglobal.booster.transform.ArtifactManager.Companion.SYMBOL_LIST_WITH_PACKAGE_NAME
import com.didiglobal.booster.transform.TransformContext
import com.didiglobal.booster.transform.asm.ClassTransformer
import com.didiglobal.booster.transform.asm.simpleName
import com.google.auto.service.AutoService
import org.gradle.api.logging.Logging
import org.objectweb.asm.Opcodes
import org.objectweb.asm.tree.ClassNode
import org.objectweb.asm.tree.FieldInsnNode
import org.objectweb.asm.tree.FieldNode
import org.objectweb.asm.tree.LdcInsnNode
import java.util.concurrent.ForkJoinPool

internal const val R_STYLEABLE = "R\$styleable"
internal const val R_STYLEABLE_CLASS = "$R_STYLEABLE.class"
internal const val ANDROID_R = "android/R$"
internal const val COM_ANDROID_INTERNAL_R = "com/android/internal/R$"

internal val CONST_TYPE_SIGNATURES = setOf("Z", "B", "C", "S", "I", "J", "F", "D", "L/java/lang/String;")

private val logger = Logging.getLogger(ShrinkTransformer::class.java)

/**
 * Represents a class node transformer for constants shrinking
 *
 * @author johnsonlee
 */
@AutoService(ClassTransformer::class)
class ShrinkTransformer : ClassTransformer {

    private lateinit var applicationId: String
    private lateinit var pkgRStyleable: String
    private lateinit var symbols: SymbolList
    private lateinit var retainedSymbols: Set<String>

    override fun onPreTransform(context: TransformContext) {
        this.applicationId = context.artifacts.get(SYMBOL_LIST_WITH_PACKAGE_NAME).single().head()!!.replace('.', '/')
        this.symbols = SymbolList.from(context.artifacts.get(SYMBOL_LIST).single())
        this.pkgRStyleable = "$applicationId/$R_STYLEABLE"

        ForkJoinPool().also { pool ->
            context.deleteLibraryRs(pool, applicationId)
            retainedSymbols = context.findRetainedSymbols(pool)
        }.shutdown()

        logger.info("Retained symbols: \n  ${retainedSymbols.joinTo(StringBuilder(), "\n  - ")}")
    }

    override fun transform(context: TransformContext, klass: ClassNode): ClassNode {
        when {
            klass.name == this.pkgRStyleable -> klass.removeIntFields()
            klass.simpleName == "BuildConfig" -> klass.removeConstantFields()
            else -> replaceSymbolReferenceWithConstant(klass)
        }
        return klass
    }

    private fun replaceSymbolReferenceWithConstant(klass: ClassNode) {
        klass.methods.forEach { method ->
            val insns = method.instructions.iterator().asIterable().filter {
                it.opcode == Opcodes.GETSTATIC
            }.map {
                it as FieldInsnNode
            }.filter {
                ("I" == it.desc || "[I" == it.desc)
                        && it.owner.substring(it.owner.lastIndexOf('/') + 1).startsWith("R$")
                        && !(it.owner.startsWith(COM_ANDROID_INTERNAL_R) || it.owner.startsWith(ANDROID_R))
            }

            val intFields = insns.filter { "I" == it.desc && !retainedSymbols.contains(it.name) }
            val intArrayFields = insns.filter { "[I" == it.desc }

            // Replace int field with constant
            intFields.forEach { field ->
                val type = field.owner.substring(field.owner.lastIndexOf("/R$") + 3)
                try {
                    method.instructions.insertBefore(field, LdcInsnNode(symbols.getInt(type, field.name)))
                    method.instructions.remove(field)
                } catch (e: NullPointerException) {
                    println("$RED ! Unresolvable symbol R.$type.${field.name} : ${klass.name}.${method.name}${method.desc} $RESET")
                }
            }

            // Replace library's R fields with application's R fields
            intArrayFields.forEach { field ->
                field.owner = "$applicationId/${field.owner.substring(field.owner.lastIndexOf('/') + 1)}"
            }
        }
    }

}

/**
 * Find symbols that should be retained, such as:
 *
 * - attribute `constraint_referenced_ids` in `ConstraintLayout`
 */
private fun TransformContext.findRetainedSymbols(pool: ForkJoinPool): Set<String> {
    return artifacts.get(MERGED_RES).map {
        pool.invoke(RetainedSymbolCollector(it))
    }.flatten().toSet()
}

/**
 * Delete R files of libraries
 */
private fun TransformContext.deleteLibraryRs(pool: ForkJoinPool, applicationId: String) {
    return artifacts.get(JAVAC).map {
        pool.invoke(RCollector(it))
    }.flatten().filter {
        // only keep application's R$styleable.class
        !it.parent.endsWith(applicationId.separatorsToSystem()) || it.name != R_STYLEABLE_CLASS
    }.forEach {
        // delete library's R
        if (it.delete()) {
            println("$YELLOW x ${it.absolutePath}$RESET")
        } else {
            println("$BLUE ? ${it.absolutePath}$RESET")
        }
    }
}

private fun ClassNode.removeIntFields() {
    fields.map {
        it as FieldNode
    }.filter {
        it.desc == "I"
    }.forEach {
        fields.remove(it)
        println("$YELLOW x $name.${it.name} : ${it.desc}$RESET")
    }
}

private fun ClassNode.removeConstantFields() {
    fields.map {
        it as FieldNode
    }.filter {
        0 != (Opcodes.ACC_STATIC and it.access)
                && 0 != (Opcodes.ACC_FINAL and it.access)
                && CONST_TYPE_SIGNATURES.contains(it.desc)
    }.forEach {
        fields.remove(it)
        println("$YELLOW x $name.${it.name} : ${it.desc}$RESET")
    }
}
