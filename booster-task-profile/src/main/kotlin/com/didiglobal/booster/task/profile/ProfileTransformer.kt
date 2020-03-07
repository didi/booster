package com.didiglobal.booster.task.profile

import com.didiglobal.booster.aapt2.Aapt2Container
import com.didiglobal.booster.aapt2.BinaryParser
import com.didiglobal.booster.aapt2.Resources
import com.didiglobal.booster.aapt2.parseAapt2Container
import com.didiglobal.booster.kotlinx.Wildcard
import com.didiglobal.booster.kotlinx.asIterable
import com.didiglobal.booster.kotlinx.descriptor
import com.didiglobal.booster.kotlinx.file
import com.didiglobal.booster.kotlinx.separatorsToSystem
import com.didiglobal.booster.kotlinx.touch
import com.didiglobal.booster.task.profile.dot.GraphType
import com.didiglobal.booster.task.profile.graph.CallGraph
import com.didiglobal.booster.task.profile.graph.CallGraph.Node
import com.didiglobal.booster.task.profile.graph.toEdges
import com.didiglobal.booster.transform.ArtifactManager
import com.didiglobal.booster.transform.TransformContext
import com.didiglobal.booster.transform.asm.ClassTransformer
import com.didiglobal.booster.transform.asm.getValue
import com.didiglobal.booster.transform.asm.isAnnotation
import com.didiglobal.booster.transform.asm.isInvisibleAnnotationPresent
import com.didiglobal.booster.util.ComponentHandler
import com.didiglobal.booster.util.search
import org.objectweb.asm.tree.ClassNode
import org.objectweb.asm.tree.MethodInsnNode
import org.objectweb.asm.tree.MethodNode
import java.lang.reflect.Modifier
import java.net.URL
import java.util.Stack
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArraySet
import javax.xml.parsers.SAXParserFactory
import kotlin.streams.asSequence

/**
 * Represents a class node transformer for static analysis
 *
 * @author johnsonlee
 */
class ProfileTransformer : ClassTransformer {

    /**
     * The call graph of each class
     */
    private val graphBuilders = mutableMapOf<String, CallGraph.Builder>()

    /**
     * The global call graph of whole project
     */
    private lateinit var globalBuilder: CallGraph.Builder

    private val ignores: MutableSet<Wildcard> = mutableSetOf(*DEFAULT_IGNORES)

    private val classesRunOnUiThread = ConcurrentHashMap<String, Class<*>>()
    private val classesRunOnMainThread = ConcurrentHashMap<String, Class<*>>()

    private val nodesRunOnUiThread = CopyOnWriteArraySet(PLATFORM_METHODS_RUN_ON_UI_THREAD)
    private val nodesRunOnMainThread = CopyOnWriteArraySet(PLATFORM_METHODS_RUN_ON_MAIN_THREAD)

    override fun onPreTransform(context: TransformContext) {
        this.graphBuilders.clear()
        this.globalBuilder = CallGraph.Builder()
        this.classesRunOnUiThread += PLATFORM_CLASSES_RUN_ON_UI_THREAD.map {
            it to Class.forName(it.replace('/', '.'), false, context.klassPool.classLoader)
        }
        this.classesRunOnMainThread += PLATFORM_CLASSES_RUN_ON_MAIN_THREAD.map {
            it to Class.forName(it.replace('/', '.'), false, context.klassPool.classLoader)
        }
        this.ignores += context.getProperty(PROPERTY_IGNORES, "").trim().split(',').map(Wildcard.Companion::valueOf)
        this.loadMainThreadEntryPoints(context)
        this.loadUiThreadEntryPoints(context)
    }

    /**
     * Find custom View as UI thread entry point by parsing layout xml
     */
    private fun loadUiThreadEntryPoints(context: TransformContext) {
        // Load platform widgets
        val widgets = context.bootClasspath.find {
            it.name == "android.jar"
        }?.parentFile?.file("data", "widgets.txt")?.readLines()?.filter {
            it.startsWith("W")
        }?.map {
            it.substring(1, it.indexOf(' '))
        }?.toSet() ?: emptySet()

        val visit: (Aapt2Container.Xml) -> Collection<Resources.XmlElement> = { xml ->
            val elements = mutableListOf<Resources.XmlElement>()
            val stack = Stack<Resources.XmlNode>().apply {
                add(xml.root)
            }

            while (stack.isNotEmpty()) {
                val node = stack.pop()
                if (node.hasElement()) {
                    elements += node.element
                    node.element.childList.forEach {
                        stack.push(it)
                    }
                }
            }

            elements
        }

        this.nodesRunOnUiThread += context.artifacts.get(ArtifactManager.MERGED_RES).search {
            it.name.startsWith("layout_") && it.name.endsWith(".xml.flat")
        }.parallelStream().map { layout ->
            BinaryParser(layout).use(BinaryParser::parseAapt2Container).entries.filterIsInstance(Aapt2Container.Xml::class.java)
        }.flatMap {
            it.parallelStream()
        }.map(visit).flatMap {
            it.parallelStream()
        }.filter {
            '.' in it.name || it.name in widgets
        }.map {
            val clazz = Class.forName(it.name, false, context.klassPool.classLoader)
            (clazz.declaredMethods + clazz.methods).toSet().filter { m ->
                Modifier.isPublic(m.modifiers) || Modifier.isProtected(m.modifiers)
            }.map { m ->
                Node(clazz.name.replace('.', '/'), m.name, m.descriptor)
            }
        }.flatMap {
            it.parallelStream()
        }.asSequence()
    }

    /**
     * Find main thread entry point by parsing AndroidManifest.xml
     */
    private fun loadMainThreadEntryPoints(context: TransformContext) {
        val parser = SAXParserFactory.newInstance().newSAXParser()
        context.artifacts.get(ArtifactManager.MERGED_MANIFESTS).forEach { manifest ->
            val handler = ComponentHandler()
            parser.parse(manifest, handler)

            // Attach component entry points to graph ROOT
            mapOf(
                    handler.applications to "android.app.Application",
                    handler.activities to "android.app.Activity",
                    handler.services to "android.app.Service",
                    handler.receivers to "android.content.BroadcastReceiver",
                    handler.providers to "android.content.ContentProvider"
            ).map { (components, type) ->
                val clazz = Class.forName(type, false, context.klassPool.classLoader)
                val entryPoints = (clazz.declaredMethods + clazz.methods).toSet().filter {
                    Modifier.isPublic(it.modifiers) || Modifier.isProtected(it.modifiers)
                }.map {
                    it.name to it.descriptor
                }.toSet()

                components.map { component ->
                    entryPoints.map {
                        Node(component.replace('.', '/'), it.first, it.second)
                    }
                }.flatten()
            }.flatten().forEach {
                globalBuilder.addEdge(CallGraph.ROOT, it)
                this.nodesRunOnMainThread += it
            }
        }
    }

    override fun transform(context: TransformContext, klass: ClassNode): ClassNode {
        if (klass.isExcluded()) {
            return klass
        }

        val clazz = Class.forName(klass.name.replace('/', '.'), false, context.klassPool.classLoader)
        val isClassRunOnUiThread = klass.isRunOnUiThread(clazz)
        val isClassRunOnMainThread = klass.isRunOnMainThread(clazz)
        val isClassRunOnUiOrMainThread = isClassRunOnMainThread || isClassRunOnUiThread

        klass.methods.filter { method ->
            this.ignores.none {
                it.matches("${klass.name}.${method.name}${method.desc}")
            }
        }.forEach { method ->
            val isMethodRunUiOrMainThread = isClassRunOnUiOrMainThread
                    || method.isRunOnUiThread(clazz)
                    || method.isRunOnMainThread(clazz)
                    || method.isSubscribeOnMainThread()

            if (isMethodRunUiOrMainThread) {
                val node = Node(klass.name, method.name, method.desc)

                if (isClassRunOnMainThread) {
                    this.nodesRunOnMainThread += node
                }

                if (isClassRunOnUiThread) {
                    this.nodesRunOnUiThread += node
                }

                globalBuilder.addEdge(CallGraph.ROOT, node)
                graphBuilders.getOrPut(klass.name) {
                    CallGraph.Builder().setTitle(klass.name.replace('/', '.'))
                }.addEdge(CallGraph.ROOT, node)
            } else if (method.isFinalizer()) {
                graphBuilders.getOrPut(klass.name) {
                    CallGraph.Builder().setTitle(klass.name.replace('/', '.'))
                }.addEdge(CallGraph.ROOT, Node(klass.name, method.name, method.desc))
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

    override fun onPostTransform(context: TransformContext) {
        val graph = globalBuilder.build()
        val apis = URL(context.getProperty(PROPERTY_APIS, DEFAULT_APIS)).openStream().bufferedReader().use {
            it.readLines().filter(String::isNotBlank).map(Node.Companion::valueOf).toSet()
        }

        // Analyse global call graph and separate each chain to individual graph
        graph[CallGraph.ROOT].forEach { node ->
            graph.analyse(context, node, listOf(CallGraph.ROOT, node), apis) { chain ->
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
            println("Generating call graph ${pair.first} ...")
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

    /**
     * Check if this class is run on main thread
     */
    private fun ClassNode.isRunOnMainThread(self: Class<*>) = isRunOnThread(self, MAIN_THREAD_ANNOTATIONS, classesRunOnMainThread)

    /**
     * Check if this class is run on UI thread
     */
    private fun ClassNode.isRunOnUiThread(self: Class<*>) = isRunOnThread(self, UI_THREAD_ANNOTATIONS, classesRunOnUiThread)

    private fun ClassNode.isRunOnThread(self: Class<*>, annotations: Array<String>, classesRunOnThread: MutableMap<String, Class<*>>): Boolean {
        // check if annotated with thread annotations
        if (isInvisibleAnnotationPresent(*annotations)) {
            classesRunOnThread += name to self
            return true
        }

        val getParents: (Class<*>) -> Set<Class<*>> = { clazz: Class<*> ->
            mutableSetOf<Class<*>>().apply {
                if (null != clazz.superclass) this += clazz.superclass
                if (null != clazz.interfaces) this += clazz.interfaces
            }
        }

        val stack = Stack<Class<*>>().apply { push(self) }

        while (stack.isNotEmpty()) {
            val clazz = stack.pop()
            val parents = getParents(clazz)

            // check if derived from other class with run on specific thread
            if (parents.any(classesRunOnThread::containsValue)) {
                classesRunOnThread += name to clazz
                return true
            }

            parents.forEach {
                stack.push(it)
            }
        }

        return false
    }

    /**
     * Check if this method is run on main thread
     */
    private fun MethodNode.isRunOnMainThread(clazz: Class<*>) = isRunOnThread(clazz, MAIN_THREAD_ANNOTATIONS, nodesRunOnMainThread)

    /**
     * Check if this method is run on UI thread
     */
    private fun MethodNode.isRunOnUiThread(clazz: Class<*>) = isRunOnThread(clazz, UI_THREAD_ANNOTATIONS, nodesRunOnUiThread)

    private fun MethodNode.isRunOnThread(clazz: Class<*>, annotations: Array<String>, nodesRunOnThread: Set<Node>): Boolean {
        if (this.isInvisibleAnnotationPresent(*annotations)) {
            return true
        }

        // check if override the method that runs on main thread
        return nodesRunOnThread.filter {
            it.name == this.name && it.desc == this.desc
        }.any {
            Class.forName(it.type.replace('/', '.'), false, clazz.classLoader).isAssignableFrom(clazz)
        }
    }
}

private fun isHit(apis: Set<Node>, target: Node, context: TransformContext) = apis.contains(target) || apis.any {
    target.name == it.name && target.desc == it.desc && context.klassPool[it.type].isAssignableFrom(target.type)
}

internal fun ClassNode.isExcluded() = isAnnotation() || methods.isEmpty() || EXCLUDES matches name

internal fun MethodNode.isFinalizer() = name == "finalize" && desc == "()V" && instructions.size() > 1

internal fun MethodNode.isSubscribeOnMainThread(): Boolean {
    return visibleAnnotations
            ?.find { it.desc == EVENTBUS_SUBSCRIBE }
            ?.getValue<Array<String>>("threadMode")
            ?.contentEquals(arrayOf(EVENTBUS_THREAD_MODE, "MAIN")) ?: false
}

private const val EVENTBUS_SUBSCRIBE = "Lorg/greenrobot/eventbus/Subscribe;"
private const val EVENTBUS_THREAD_MODE = "Lorg/greenrobot/eventbus/ThreadMode;"

private val PROPERTY_PREFIX = Build.ARTIFACT.replace('-', '.')

private val PROPERTY_APIS = "$PROPERTY_PREFIX.apis"

internal val DEFAULT_APIS = ProfileTransformer::class.java.classLoader.getResource("profile-apis.txt")!!.toString()

private val PROPERTY_IGNORES = "$PROPERTY_PREFIX.ignores"

private val DEFAULT_IGNORES = arrayOf(
        "android/*",
        "androidx/*",
        "com/android/*"
).map(Wildcard.Companion::valueOf).toTypedArray()
