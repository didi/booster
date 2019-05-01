package com.didiglobal.booster.transform.lint

import com.didiglobal.booster.kotlinx.MAGENTA
import com.didiglobal.booster.kotlinx.RESET
import com.didiglobal.booster.kotlinx.asIterable
import com.didiglobal.booster.transform.ArtifactManager
import com.didiglobal.booster.transform.TransformContext
import com.didiglobal.booster.transform.asm.ClassTransformer
import com.didiglobal.booster.transform.lint.graph.CallGraph
import com.didiglobal.booster.transform.lint.graph.CallGraph.Node
import com.didiglobal.booster.util.ComponentHandler
import com.google.auto.service.AutoService
import org.objectweb.asm.tree.ClassNode
import org.objectweb.asm.tree.MethodInsnNode
import org.objectweb.asm.tree.MethodNode
import java.io.File
import java.net.URI
import java.util.stream.Collectors
import javax.xml.parsers.SAXParserFactory

/**
 * Represents a class node transformer for static analysis
 *
 * @author johnsonlee
 */
@AutoService(ClassTransformer::class)
class LintTransformer : ClassTransformer {

    private val builder = CallGraph.Builder()

    override fun onPreTransform(context: TransformContext) {
        val parser = SAXParserFactory.newInstance().newSAXParser()
        context.artifacts.get(ArtifactManager.MERGED_MANIFESTS).forEach { manifest ->
            val handler = ComponentHandler()
            parser.parse(manifest, handler)

            // Attach component entry points to graph ROOT
            mapOf(
                    Pair(handler.applications, APPLICATION_ENTRY_POINTS),
                    Pair(handler.activities, ACTIVITY_ENTRY_POINTS),
                    Pair(handler.services, SERVICE_ENTRY_POINTS),
                    Pair(handler.receivers, RECEIVER_ENTRY_POINTS),
                    Pair(handler.providers, PROVIDER_ENTRY_POINTS)
            ).forEach { components, entryPoints ->
                components.forEach { component ->
                    entryPoints.map {
                        CallGraph.Node(component.replace('.', '/'), it.name, it.desc)
                    }.forEach {
                        builder.addEdge(CallGraph.ROOT, it)
                    }
                }
            }
        }
    }

    override fun transform(context: TransformContext, klass: ClassNode): ClassNode {
        if (klass.isIgnored) {
            return klass
        }

        klass.methods.forEach { method ->
            when {
                // any method signature is sensitive
                method.isSensitive -> {
                    builder.addEdge(CallGraph.ROOT, CallGraph.Node(klass.name, method.name, method.desc))
                }
                // finalizer is not reliable in Android
                method.name == "finalize" && method.desc == "()V" && method.instructions.size() > 1 && !klass.name.startsWith("com/didiglobal/booster/instrument/") -> {
                    println(" ⚠️  $MAGENTA${klass.name}.${method.name}${method.desc}$RESET")
                }
            }

            // construct call graph by scanning INVOKE* instructions
            method.instructions.iterator().asIterable().filterIsInstance(MethodInsnNode::class.java).forEach { invoke ->
                val from = CallGraph.Node(klass.name, method.name, method.desc)
                val to = CallGraph.Node(invoke.owner, invoke.name, invoke.desc)

                // break circular invocation
                if (!builder.hasEdge(to, from)) {
                    builder.addEdge(from, to)
                }
            }
        }
        return klass
    }

    override fun onPostTransform(context: TransformContext) {
        val apis = context.lintApis
        val graph = builder.build()
        graph.edges[CallGraph.ROOT]?.forEach { entryPoint ->
            graph.analyse(context, entryPoint, listOf(entryPoint), apis)
        }
    }

}

private val ClassNode.isIgnored: Boolean
    get() = IGNORES.any {
        this.name.startsWith(it)
    }

private val MethodNode.isSensitive: Boolean
    get() = SENSITIVE_WORDS.any {
        this.desc.contains(it, true)
    }

private val TransformContext.lintApis: Set<Node>
    get() {
        return if (hasProperty(PROPERTY_LINT_APIS)) {
            val uri = URI(this.getProperty(PROPERTY_LINT_APIS))
            val url = if (uri.isAbsolute) uri.toURL() else File(uri).toURI().toURL()

            url.openStream().bufferedReader().use {
                return it.lines().filter(String::isNotBlank).map { line ->
                    Node.valueOf(line.trim())
                }.collect(Collectors.toSet())
            }
        } else LINT_APIS
    }

private fun CallGraph.analyse(context: TransformContext, node: Node, parent: List<Node>, apis: Set<Node>) {
    this.edges[node]?.forEach { target ->
        // break circular invocation
        if (parent.contains(target)) {
            return
        }

        val paths = parent.plus(target)
        if (apis.contains(target) || apis.any { target.name == it.name && target.desc == it.desc && context.klassPool.get(it.type).isAssignableFrom(target.type) }) {
            println(" ⚠️  $MAGENTA${paths.joinToString("$RESET -> $MAGENTA")} $RESET")
            return
        }
        analyse(context, target, paths, apis)
    }
}

private val PROPERTY_LINT_APIS = "${Build.ARTIFACT.replace('-', '.')}.apis"

