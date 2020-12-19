package com.didiglobal.booster.transform.usage

import com.didiglobal.booster.kotlinx.CSI_RESET
import com.didiglobal.booster.kotlinx.CSI_YELLOW
import com.didiglobal.booster.transform.TransformContext
import com.didiglobal.booster.transform.asm.ClassTransformer
import com.google.auto.service.AutoService
import org.objectweb.asm.tree.ClassNode
import org.objectweb.asm.tree.MethodInsnNode
import java.net.URL

/**
 * Represents a class node transformer for type/method/field usage analysis
 *
 * @author johnsonlee
 */
@AutoService(ClassTransformer::class)
class UsageTransformer : ClassTransformer {

    private lateinit var usedApis: Set<String>

    override val name: String = Build.ARTIFACT

    override fun onPreTransform(context: TransformContext) {
        this.usedApis = context.getProperty<String?>(PROPERTY_USED_APIS, null)?.let { uri ->
            URL(uri).openStream().bufferedReader().use {
                it.readLines().filter(String::isNotBlank).map { line ->
                    line.trim()
                }.toSet()
            }
        } ?: emptySet()
    }

    override fun transform(context: TransformContext, klass: ClassNode): ClassNode {
        if (this.usedApis.isNotEmpty()) {
            klass.methods.forEach { method ->
                method.instructions.iterator().asSequence().filterIsInstance(MethodInsnNode::class.java).map {
                    "${it.owner}.${it.name}${it.desc}"
                }.filter {
                    this.usedApis.contains(it)
                }.forEach {
                    println("$CSI_YELLOW ! ${klass.name}.${method.name}${method.desc}: $CSI_RESET$it")
                }
            }
        }

        return klass
    }

}

private val PROPERTY_USED_APIS = "${Build.ARTIFACT.replace('-', '.')}.apis"

internal val DEFAULT_USED_APIS = UsageTransformer::class.java.classLoader.getResource("used-apis.txt")!!.toString()
