package com.didiglobal.booster.transform.shrink

import com.didiglobal.booster.kotlinx.BLUE
import com.didiglobal.booster.kotlinx.RED
import com.didiglobal.booster.kotlinx.RESET
import com.didiglobal.booster.kotlinx.YELLOW
import com.didiglobal.booster.kotlinx.asIterable
import com.didiglobal.booster.kotlinx.head
import com.didiglobal.booster.kotlinx.separatorsToSystem
import com.didiglobal.booster.transform.ArtifactManager.Companion.JAVAC
import com.didiglobal.booster.transform.ArtifactManager.Companion.SYMBOL_LIST
import com.didiglobal.booster.transform.ArtifactManager.Companion.SYMBOL_LIST_WITH_PACKAGE_NAME
import com.didiglobal.booster.transform.TransformContext
import com.didiglobal.booster.transform.asm.ClassTransformer
import com.didiglobal.booster.transform.asm.simpleName
import com.google.auto.service.AutoService
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

/**
 * Represents a class node transformer for constants shrinking
 *
 * @author johnsonlee
 */
@AutoService(ClassTransformer::class)
class ShrinkTransformer : ClassTransformer {

    lateinit var pkg: String
    lateinit var pkgRStyleable: String
    lateinit var symbols: SymbolList

    override fun onPreTransform(context: TransformContext) {
        this.pkg = context.artifacts.get(SYMBOL_LIST_WITH_PACKAGE_NAME).single().head()!!.replace('.', '/')
        this.symbols = SymbolList.from(context.artifacts.get(SYMBOL_LIST).single())
        this.pkgRStyleable = "$pkg/$R_STYLEABLE"

        ForkJoinPool().also { pool ->
            context.artifacts.get(JAVAC).forEach { root ->
                pool.invoke(RFinder(root)).filter {
                    // only keep application's R$styleable.class
                    !it.parent.endsWith(pkg.separatorsToSystem()) || it.name != R_STYLEABLE_CLASS
                }.forEach {
                    // delete library's R
                    if (it.delete()) {
                        println("$YELLOW x ${it.absolutePath}$RESET")
                    } else {
                        println("$BLUE ? ${it.absolutePath}$RESET")
                    }
                }
            }
        }.shutdown()
    }

    override fun transform(context: TransformContext, klass: ClassNode): ClassNode {
        when {
            klass.name == this.pkgRStyleable -> removeIntFields(klass)
            klass.simpleName == "BuildConfig" -> removeConstantFields(klass)
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

            val intFields = insns.filter { "I" == it.desc }
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
                field.owner = "$pkg/${field.owner.substring(field.owner.lastIndexOf('/') + 1)}"
            }
        }
    }

}

private fun removeIntFields(klass: ClassNode) {
    klass.fields.map {
        it as FieldNode
    }.filter {
        it.desc == "I"
    }.forEach {
        klass.fields.remove(it)
        println("$YELLOW x ${klass.name}.${it.name} : ${it.desc}$RESET")
    }
}

private fun removeConstantFields(klass: ClassNode) {
    klass.fields.map {
        it as FieldNode
    }.filter {
        0 != (Opcodes.ACC_STATIC and it.access)
                && 0 != (Opcodes.ACC_FINAL and it.access)
                && CONST_TYPE_SIGNATURES.contains(it.desc)
    }.forEach {
        klass.fields.remove(it)
        println("$YELLOW x ${klass.name}.${it.name} : ${it.desc}$RESET")
    }
}
