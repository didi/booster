package com.didiglobal.booster.transform.usage

import com.didiglobal.booster.kotlinx.CSI_RESET
import com.didiglobal.booster.kotlinx.CSI_YELLOW
import com.didiglobal.booster.transform.TransformContext
import com.didiglobal.booster.transform.asm.ClassTransformer
import com.google.auto.service.AutoService
import org.objectweb.asm.tree.ClassNode
import org.objectweb.asm.tree.MethodInsnNode
import java.io.File
import java.net.URI
import java.util.stream.Collectors

/**
 * Represents a class node transformer for type/method/field usage analysis
 *
 * @author johnsonlee
 */
@AutoService(ClassTransformer::class)
class UsageTransformer : ClassTransformer {

    override fun transform(context: TransformContext, klass: ClassNode): ClassNode {
        if (context.hasProperty(PROPERTY_USED_APIS)) {
            val apis = context.usedApis

            klass.methods.forEach { method ->
                method.instructions.iterator().asSequence().filterIsInstance(MethodInsnNode::class.java).map {
                    "${it.owner}.${it.name}${it.desc}"
                }.filter {
                    apis.contains(it)
                }.forEach {
                    println("$CSI_YELLOW ! ${klass.name}.${method.name}${method.desc}: $CSI_RESET$it")
                }
            }
        }

        return klass
    }

}

private val TransformContext.usedApis: Set<String>
    get() {
        val uri = URI(this.getProperty(PROPERTY_USED_APIS))
        val url = if (uri.isAbsolute) uri.toURL() else File(uri).toURI().toURL()

        url.openStream().bufferedReader().use {
            return it.lines().filter(String::isNotBlank).map { line ->
                line.trim()
            }.collect(Collectors.toSet())
        }
    }

private val PROPERTY_USED_APIS = "${Build.ARTIFACT.replace('-', '.')}.apis"
