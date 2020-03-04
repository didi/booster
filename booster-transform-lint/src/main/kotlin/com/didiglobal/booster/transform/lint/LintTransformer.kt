package com.didiglobal.booster.transform.lint

import com.didiglobal.booster.kotlinx.Wildcard
import com.didiglobal.booster.kotlinx.asIterable
import com.didiglobal.booster.kotlinx.file
import com.didiglobal.booster.kotlinx.separatorsToSystem
import com.didiglobal.booster.kotlinx.touch
import com.didiglobal.booster.transform.ArtifactManager
import com.didiglobal.booster.transform.TransformContext
import com.didiglobal.booster.transform.asm.ClassTransformer
import com.didiglobal.booster.transform.asm.getValue
import com.didiglobal.booster.transform.asm.isInvisibleAnnotationPresent
import com.didiglobal.booster.transform.lint.dot.GraphType
import com.didiglobal.booster.transform.lint.graph.CallGraph
import com.didiglobal.booster.transform.lint.graph.CallGraph.Node
import com.didiglobal.booster.transform.lint.graph.toEdges
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

    /**
     * The lint graph
     */
    private val graphBuilders = mutableMapOf<String, CallGraph.Builder>()

    /**
     * The global call graph
     */
    private lateinit var globalBuilder: CallGraph.Builder

    private val ignores: MutableSet<Wildcard> = mutableSetOf(*DEFAULT_PROPERTY_IGNORES_DEFAULT)

    override fun onPreTransform(context: TransformContext) {
        this.graphBuilders.clear()
        this.globalBuilder = CallGraph.Builder()
        this.ignores += context.getProperty(PROPERTY_IGNORES)?.split(',')?.map(Wildcard.Companion::valueOf) ?: emptySet()

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
            ).forEach { (components, entryPoints) ->
                components.forEach { component ->
                    entryPoints.map {
                        CallGraph.Node(component.replace('.', '/'), it.name, it.desc)
                    }.forEach {
                        globalBuilder.addEdge(CallGraph.ROOT, it)
                    }
                }
            }
        }
    }

    override fun transform(context: TransformContext, klass: ClassNode): ClassNode {
        if (klass.isExcluded()) {
            return klass
        }

        val klassDefinitelyRunOnMainThread = klass.isDefinitelyRunOnMainThread(context)

        klass.methods.filter { method ->
            this.ignores.none {
                it.matches("${klass.name}.${method.name}${method.desc}")
            }
        }.forEach { method ->
            when {
                klassDefinitelyRunOnMainThread || method.isDefinitelyRunOnMainThread() -> {
                    buildCallGraph(klass, method)
                }
                method.isFinalizer() -> {
                    buildCallGraph(klass, method)
                }
                method.isMostLikelyRunOnMainThread() -> {
                    globalBuilder.addEdge(CallGraph.ROOT, Node(klass.name, method.name, method.desc))
                }
            }

            // construct call graph by scanning INVOKE* instructions
            method.instructions.iterator().asIterable().filterIsInstance(MethodInsnNode::class.java).filter { invoke ->
                this.ignores.none {
                    it.matches("${invoke.owner}.${invoke.name}${invoke.desc}")
                }
            }.forEach { invoke ->
                val to = Node(invoke.owner, invoke.name, invoke.desc)
                val from = Node(klass.name, method.name, method.desc)

                // break circular invocation
                if (!globalBuilder.hasEdge(to, from)) {
                    globalBuilder.addEdge(from, to)
                }
            }
        }

        return klass
    }

    private fun buildCallGraph(klass: ClassNode, method: MethodNode) {
        graphBuilders.getOrPut(klass.name) {
            CallGraph.Builder().setTitle(klass.name.replace('/', '.'))
        }.addEdge(CallGraph.ROOT, Node(klass.name, method.name, method.desc))
    }

    override fun onPostTransform(context: TransformContext) {
        val graph = globalBuilder.build()
        val lints = if (context.hasProperty(PROPERTY_APIS)) {
            val uri = URI(context.getProperty(PROPERTY_APIS)!!)
            val url = if (uri.isAbsolute) uri.toURL() else File(uri).toURI().toURL()

            url.openStream().bufferedReader().use {
                it.lines().filter(String::isNotBlank).map { line ->
                    Node.valueOf(line.trim())
                }.collect(Collectors.toSet())
            }
        } else LINT_APIS

        // Analyse global call graph and separate each chain to individual graph
        graph[CallGraph.ROOT].forEach { node ->
            graph.analyse(context, node, listOf(CallGraph.ROOT, node), lints) { chain ->
                val builder = graphBuilders.getOrPut(node.type) {
                    CallGraph.Builder().setTitle(node.type.replace('/', '.'))
                }
                chain.toEdges().forEach { edge ->
                    builder.addEdges(edge)
                }
            }
        }

        // Print individual call graph into dot file
        graphBuilders.map {
            Pair(context.reportsDir.file(Build.ARTIFACT).file(context.name).file(it.key.separatorsToSystem() + ".dot"), it.value.build())
        }.parallelStream().forEach { pair ->
            pair.first.touch().printWriter().use { printer ->
                pair.second.print(printer, GraphType.DIGRAPH::format)
            }
        }
    }

    /**
     * Analyse from *node* recursively
     *
     * @param context The transform context
     * @param node The entry point
     * @param chain The call chain
     * @param apis The apis to lint
     */
    private fun CallGraph.analyse(context: TransformContext, node: Node, chain: List<Node>, apis: Set<Node>, action: (List<Node>) -> Unit) {
        this[node].forEach loop@{ target ->
            // break circular invocation
            if (chain.contains(target)) {
                return@loop
            }

            val newChain = chain.plus(target)
            if (isHit(apis, target, context)) {
                action(newChain)
                return@loop
            }

            analyse(context, target, newChain, apis, action)
        }
    }

}

private fun isHit(apis: Set<Node>, target: Node, context: TransformContext): Boolean {
    return apis.contains(target) || apis.any {
        target.name == it.name && target.desc == it.desc && context.klassPool[it.type].isAssignableFrom(target.type)
    }
}

private fun ClassNode.isExcluded(): Boolean {
    return this.name.startsWith("com/didiglobal/booster/instrument/") || IGNORES.any {
        this.name.startsWith(it)
    }
}

private fun MethodNode.isFinalizer() = name == "finalize" && desc == "()V" && instructions.size() > 1

private fun MethodNode.isMostLikelyRunOnMainThread(): Boolean = SENSITIVES.any {
    this.desc.contains(it, true)
}

private fun MethodNode.isSubscribeOnMainThread() = this.visibleAnnotations?.find {
    it.desc == "Lorg/greenrobot/eventbus/Subscribe;"
}?.getValue<Array<String>>("threadMode")?.contentEquals(arrayOf("Lorg/greenrobot/eventbus/ThreadMode;", "MAIN")) ?: false

private fun MethodNode.isDefinitelyRunOnMainThread(): Boolean {
    return isInvisibleAnnotationPresent(*MAIN_THREAD_ANNOTATIONS) || isSubscribeOnMainThread()
}

private fun ClassNode.isDefinitelyRunOnMainThread(context: TransformContext): Boolean {
    return isInvisibleAnnotationPresent(*MAIN_THREAD_ANNOTATIONS)
            || CLASSES_RUN_ON_MAIN_THREAD.any { context.klassPool[it].isAssignableFrom(name) }
}

private val PROPERTY_PREFIX = Build.ARTIFACT.replace('-', '.')

private val PROPERTY_APIS = "$PROPERTY_PREFIX.apis"

private val PROPERTY_IGNORES = "$PROPERTY_PREFIX.ignores"

private val DEFAULT_PROPERTY_IGNORES_DEFAULT = arrayOf(
        "android/*",
        "androidx/*",
        "com/android/*",
        "android/util/Log.getStackTraceString*"
).map(Wildcard.Companion::valueOf).toTypedArray()
