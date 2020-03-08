package com.didiglobal.booster.task.profile

import com.didiglobal.booster.aapt2.metadata
import com.didiglobal.booster.kotlinx.NCPU
import com.didiglobal.booster.kotlinx.Wildcard
import com.didiglobal.booster.kotlinx.asIterable
import com.didiglobal.booster.kotlinx.descriptor
import com.didiglobal.booster.kotlinx.file
import com.didiglobal.booster.kotlinx.format
import com.didiglobal.booster.kotlinx.search
import com.didiglobal.booster.kotlinx.separatorsToSystem
import com.didiglobal.booster.kotlinx.touch
import com.didiglobal.booster.task.profile.dot.GraphType
import com.didiglobal.booster.task.profile.graph.CallGraph
import com.didiglobal.booster.task.profile.graph.CallGraph.Node
import com.didiglobal.booster.task.profile.graph.toEdges
import com.didiglobal.booster.transform.ArtifactManager
import com.didiglobal.booster.transform.TransformContext
import com.didiglobal.booster.transform.asm.AsmTransformer
import com.didiglobal.booster.transform.asm.ClassTransformer
import com.didiglobal.booster.transform.asm.getValue
import com.didiglobal.booster.transform.asm.isAnnotation
import com.didiglobal.booster.transform.asm.isInvisibleAnnotationPresent
import com.didiglobal.booster.transform.asm.isNative
import com.didiglobal.booster.transform.asm.isProtected
import com.didiglobal.booster.transform.asm.isPublic
import com.didiglobal.booster.transform.util.ComponentHandler
import com.didiglobal.booster.transform.util.transform
import org.objectweb.asm.tree.ClassNode
import org.objectweb.asm.tree.MethodInsnNode
import org.objectweb.asm.tree.MethodNode
import java.io.File
import java.io.FileNotFoundException
import java.lang.reflect.Modifier.isNative
import java.lang.reflect.Modifier.isProtected
import java.lang.reflect.Modifier.isPublic
import java.net.URL
import java.util.Date
import java.util.Stack
import java.util.concurrent.Callable
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArraySet
import java.util.concurrent.Executors
import java.util.concurrent.Future
import java.util.concurrent.TimeUnit
import java.util.jar.JarFile
import javax.xml.parsers.SAXParserFactory

/**
 * Represents a class node transformer for static analysis
 *
 * @author johnsonlee
 */
class ProfileTransformer : ClassTransformer {

    private lateinit var androidJar: File
    /**
     * The global call graph of whole project
     */
    private val globalBuilder = CallGraph.Builder()

    /**
     * The call graph of each class
     */
    private val graphBuilders = mutableMapOf<String, CallGraph.Builder>()

    private val ignores: MutableSet<Wildcard> = mutableSetOf(*DEFAULT_IGNORES)

    private val classesRunOnUiThread = ConcurrentHashMap<String, Class<*>>()
    private val classesRunOnMainThread = ConcurrentHashMap<String, Class<*>>()

    private val nodesRunOnUiThread = CopyOnWriteArraySet(PLATFORM_METHODS_RUN_ON_UI_THREAD)
    private val nodesRunOnMainThread = CopyOnWriteArraySet(PLATFORM_METHODS_RUN_ON_MAIN_THREAD)

    override fun onPreTransform(context: TransformContext) {
        this.androidJar = context.bootClasspath.find {
            it.name == "android.jar"
        } ?: throw FileNotFoundException("android.jar")
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

        val visit: (File) -> Set<String> = { xml ->
            val handler = LayoutHandler()
            SAXParserFactory.newInstance().newSAXParser().parse(xml, handler)
            handler.views
        }

        val executor = Executors.newFixedThreadPool(NCPU)

        try {
            context.artifacts.get(ArtifactManager.MERGED_RES).search {
                it.name.startsWith("layout_") && it.name.endsWith(".xml.flat")
            }.map { flat ->
                executor.submit(Callable<Set<Node>> {
                    val header = flat.metadata
                    val xml = header.sourceFile

                    println("${t()} Parsing ${header.resourcePath} ...")

                    visit(xml).filter {
                        '.' in it || it in widgets // ignore system widgets
                    }.mapNotNull { tag ->
                        try {
                            val clazz = Class.forName(tag, false, context.klassPool.classLoader)

                            (clazz.declaredMethods + clazz.methods).toSet().filter { m ->
                                (isPublic(m.modifiers) || isProtected(m.modifiers)) && !isNative(m.modifiers)
                            }.map { m ->
                                Node(clazz.name.replace('.', '/'), m.name, m.descriptor)
                            }
                        } catch (e: ClassNotFoundException) {
                            System.err.println("Class ${e.localizedMessage} not found in ${header.resourcePath}")
                            null
                        } catch (e: Throwable) {
                            null
                        }
                    }.flatten().toSet()
                })
            }.forEach { future ->
                this.nodesRunOnUiThread + future.get()
            }
        } finally {
            executor.shutdown()
            executor.awaitTermination(1L, TimeUnit.HOURS)
        }
    }

    /**
     * Find main thread entry point by parsing AndroidManifest.xml
     */
    private fun loadMainThreadEntryPoints(context: TransformContext) {
        val handler = context.artifacts.get(ArtifactManager.MERGED_MANIFESTS).map { manifest ->
            ComponentHandler().also { handler ->
                SAXParserFactory.newInstance().newSAXParser().parse(manifest, handler)
            }
        }.fold(ComponentHandler()) { acc, i ->
            acc.applications += i.applications
            acc.activities += i.activities
            acc.services += i.services
            acc.receivers += i.receivers
            acc.providers += i.providers
            acc
        }

        JarFile(this.androidJar).use { jar ->
            val executor = Executors.newFixedThreadPool(NCPU)
            val futures = mutableListOf<Future<Unit>>()

            try {
                // Attach component entry points to graph ROOT
                mapOf(
                        "android/app/Application" to handler.applications,
                        "android/app/Activity" to handler.activities,
                        "android/app/Service" to handler.services,
                        "android/content/BroadcastReceiver" to handler.receivers,
                        "android/content/ContentProvider" to handler.providers
                ).forEach { (type, components) ->
                    futures += executor.submit(Callable {
                        val classes = Stack<String>().apply {
                            push(type)
                        }

                        println("${t()} Loading class ${type.replace('/', '.')} ...")

                        while (classes.isNotEmpty()) {
                            val name = classes.pop()

                            jar.getInputStream(jar.getJarEntry("${name}.class")).use { input ->
                                input.transform { bytecode ->
                                    AsmTransformer.transform(bytecode) { klass ->
                                        if ("java/lang/Object" != klass.superName) {
                                            classes.push(klass.superName)
                                        }

                                        klass.methods.filter {
                                            (it.isPublic || it.isProtected) && !it.isNative
                                        }.forEach { method ->
                                            components.forEach { component ->
                                                val node = Node(component, method.name, method.desc)
                                                globalBuilder.addEdge(CallGraph.ROOT, node)
                                                this.nodesRunOnMainThread += node
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    })
                }

                futures.forEach {
                    it.get()
                }
            } finally {
                executor.shutdown()
                executor.awaitTermination(1L, TimeUnit.HOURS)
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

internal fun ClassNode.isExcluded() = isAnnotation || methods.isEmpty() || EXCLUDES matches name

internal fun MethodNode.isFinalizer() = name == "finalize" && desc == "()V" && instructions.size() > 1

internal fun MethodNode.isSubscribeOnMainThread(): Boolean {
    return visibleAnnotations
            ?.find { it.desc == EVENTBUS_SUBSCRIBE }
            ?.getValue<Array<String>>("threadMode")
            ?.contentEquals(arrayOf(EVENTBUS_THREAD_MODE, "MAIN")) ?: false
}

private fun t() = Date().format("yyyy-MM-dd HH:mm:ss.SSS")

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
