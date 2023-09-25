package com.didiglobal.booster.task.analyser.performance

import com.didiglobal.booster.aapt2.metadata
import com.didiglobal.booster.cha.ClassHierarchy
import com.didiglobal.booster.cha.ClassSet
import com.didiglobal.booster.cha.JAVA_LANG_OBJECT
import com.didiglobal.booster.cha.asm.from
import com.didiglobal.booster.cha.fold
import com.didiglobal.booster.cha.graph.CallNode
import com.didiglobal.booster.cha.graph.ROOT
import com.didiglobal.booster.graph.Graph
import com.didiglobal.booster.graph.dot.DotGraph
import com.didiglobal.booster.kotlinx.NCPU
import com.didiglobal.booster.kotlinx.asIterable
import com.didiglobal.booster.kotlinx.file
import com.didiglobal.booster.kotlinx.green
import com.didiglobal.booster.kotlinx.red
import com.didiglobal.booster.kotlinx.search
import com.didiglobal.booster.kotlinx.separatorsToSystem
import com.didiglobal.booster.kotlinx.yellow
import com.didiglobal.booster.task.analyser.Build
import com.didiglobal.booster.mergeIdentifiedSourceSetFiles
import com.didiglobal.booster.relativeResourcePathToAbsolutePath
import com.didiglobal.booster.transform.ArtifactManager
import com.didiglobal.booster.transform.asm.args
import com.didiglobal.booster.transform.asm.className
import com.didiglobal.booster.transform.asm.getValue
import com.didiglobal.booster.transform.asm.isAbstract
import com.didiglobal.booster.transform.asm.isAnnotation
import com.didiglobal.booster.transform.asm.isInterface
import com.didiglobal.booster.transform.asm.isInvisibleAnnotationPresent
import com.didiglobal.booster.transform.asm.isNative
import com.didiglobal.booster.transform.asm.isProtected
import com.didiglobal.booster.transform.asm.isPublic
import com.didiglobal.booster.transform.asm.isStatic
import com.didiglobal.booster.transform.util.ComponentHandler
import org.objectweb.asm.tree.ClassNode
import org.objectweb.asm.tree.MethodInsnNode
import org.objectweb.asm.tree.MethodNode
import java.io.File
import java.net.URL
import java.util.concurrent.Callable
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArraySet
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.Future
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger
import javax.xml.parsers.SAXParserFactory
import kotlin.collections.component1
import kotlin.collections.component2
import kotlin.collections.set

/**
 * @author johnsonlee
 */
class PerformanceAnalyser(
        private val providedClasspath: Collection<File>,
        private val compileClasspath: Collection<File>,
        private val sourceMapFiles: Collection<File>,
        private val artifacts: ArtifactManager,
        private val properties: Map<String, *> = emptyMap<String, Any>()
) {

    private val providedClasses = providedClasspath.map {
        ClassSet.from(it)
    }.fold()

    private val compileClasses = compileClasspath.map {
        ClassSet.from(it)
    }.fold()

    private val classes = providedClasses + compileClasses

    private val unresolvedClasses = mutableSetOf<String>()

    private val hierarchy = ClassHierarchy(classes) {
        unresolvedClasses += it
    }

    /**
     * The global call graph of whole project
     */
    private val globalBuilder = Graph.Builder<CallNode>()

    /**
     * The call graph of each class
     */
    private val graphBuilders = ConcurrentHashMap<String, Graph.Builder<CallNode>>()

    private val classesRunOnUiThread = ConcurrentHashMap<String, ClassNode>()
    private val classesRunOnMainThread = ConcurrentHashMap<String, ClassNode>()

    private val nodesRunOnUiThread = CopyOnWriteArraySet(PLATFORM_METHODS_RUN_ON_UI_THREAD)
    private val nodesRunOnMainThread = CopyOnWriteArraySet(PLATFORM_METHODS_RUN_ON_MAIN_THREAD)

    private val blacklist = URL(properties[PROPERTY_BLACKLIST]?.toString() ?: VALUE_BLACKLIST).openStream().bufferedReader().use {
        it.readLines().filter(String::isNotBlank).map(CallNode.Companion::valueOf).toSet()
    }

    private val whitelist = URL(properties[PROPERTY_WHITELIST]?.toString() ?: VALUE_WHITELIST).openStream().bufferedReader().use {
        it.readLines().filter(String::isNotBlank).map(CallNode.Companion::valueOf).toSet()
    }

    private val mainThreadAnnotations: Set<String> by lazy {
        MAIN_THREAD_ANNOTATIONS.filter(classes::contains).map(::descriptor).toSet()
    }

    private val uiThreadAnnotations: Set<String> by lazy {
        UI_THREAD_ANNOTATIONS.filter(classes::contains).map(::descriptor).toSet()
    }

    private val sourcesMap:Map<String, String> by lazy {
        mergeIdentifiedSourceSetFiles(sourceMapFiles)
    }

    constructor(platform: File, compileClasspath: Collection<File>, sourceMap: Collection<File>, artifacts: ArtifactManager, properties: Map<String, *> = emptyMap<String, Any>())
            : this(platform.bootClasspath, compileClasspath, sourceMap, artifacts, properties)

    fun analyse(output: File) {
        this.classes.load().use {
            this.loadEntryPoints()
            this.analyse()
            this.dump(output)
            this.unresolvedClasses.forEach {
                println("Unresolved class ${red(it.replace('/', '.'))}")
            }
        }
    }

    private fun analyse() {
        val classes = this.compileClasses.parallelStream().filter(ClassNode::isInclude).toList()
        val index = AtomicInteger(0)
        val count = classes.size
        val executor = Executors.newFixedThreadPool(NCPU)

        try {
            classes.map {
                executor.submit {
                    val t0 = System.currentTimeMillis()
                    this.analyse(it)
                    println("${green(String.format("%3d%%", index.incrementAndGet() * 100 / count))} Analyse class ${it.className} in ${yellow(System.currentTimeMillis() - t0)} ms")
                }
            }.forEach {
                it.get()
            }
        } finally {
            executor.shutdown()
            executor.awaitTermination(1L, TimeUnit.HOURS)
        }
    }

    private fun loadEntryPoints() {
        val executor = Executors.newFixedThreadPool(NCPU)

        try {
            (loadMainThreadEntryPoints(executor) + loadUiThreadEntryPoints(executor)).forEach {
                it.get()
            }
        } finally {
            executor.shutdown()
            executor.awaitTermination(1L, TimeUnit.HOURS)
        }
    }

    /**
     * Find main thread entry point by parsing AndroidManifest.xml
     */
    private fun loadMainThreadEntryPoints(executor: ExecutorService): List<Future<*>> {
        val handler = this.artifacts.get(ArtifactManager.MERGED_MANIFESTS).map { manifest ->
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

        return arrayOf(
                "android/app/Application" to handler.applications,
                "android/app/Activity" to handler.activities,
                "android/app/Service" to handler.services,
                "android/content/BroadcastReceiver" to handler.receivers,
                "android/content/ContentProvider" to handler.providers
        ).map {
            executor.submit(Callable<Triple<ClassNode, Collection<String>, Collection<MethodNode>>> {
                val clazz = this.hierarchy[it.first] ?: throw ClassNotFoundException(it.first.replace('/', '.'))
                Triple(clazz, it.second.map { it.replace('.', '/') }, clazz.methods.filter(MethodNode::isEntryPoint))
            })
        }.map { future ->
            val (clazz, components, entryPoints) = future.get()

            classesRunOnMainThread[clazz.name] = clazz

            components.map { component ->
                executor.submit {
                    println("Loading main thread entry points from $component ...")

                    val nodes = entryPoints.map {
                        CallNode(clazz.name, it.name, it.desc)
                    }

                    this.hierarchy[component]?.run {
                        classesRunOnMainThread[component] = this
                    }
                    this.globalBuilder.addEdges(ROOT, nodes)
                    this.nodesRunOnMainThread += nodes
                }
            }
        }.flatten()
    }

    /**
     * Find custom View as UI thread entry point by parsing layout xml
     */
    private fun loadUiThreadEntryPoints(executor: ExecutorService): List<Future<*>> {
        // Load platform widgets
        val widgets = this.providedClasspath.find {
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
        return this.artifacts.get(ArtifactManager.MERGED_RES).search {
            it.name.startsWith("layout_") && it.name.endsWith(".xml.flat")
        }.map { flat ->
            executor.submit {
                val header = flat.metadata
                val xml = File(relativeResourcePathToAbsolutePath(header.sourcePath, sourcesMap))

                println("Parsing ${header.resourcePath} ...")

                this.nodesRunOnUiThread += visit(xml).filter {
                    '.' in it || it in widgets // ignore system widgets
                }.map { tag ->
                    val desc = tag.replace('.', '/')
                    val clazz = this.hierarchy[desc]

                    if (null == clazz) {
                        println(red("Unresolved class ${tag}: ${header.resourceName} -> ${header.sourcePath}"))
                        emptyList()
                    } else {
                        classesRunOnUiThread[desc] = clazz

                        hierarchy.getSuperTypes(clazz).filter {
                            it.name != JAVA_LANG_OBJECT
                        }.forEach {
                            classesRunOnUiThread[it.name] = it
                        }

                        val nodes = clazz.methods.filter(MethodNode::isEntryPoint).map { m ->
                            CallNode(clazz.name, m.name, m.desc)
                        }

                        globalBuilder.addEdges(ROOT, nodes)
                        nodes
                    }
                }.flatten()
            }
        }
    }

    private fun analyse(clazz: ClassNode) {
        val isClassRunOnUiThread = clazz.isRunOnUiThread()
        val isClassRunOnMainThread = clazz.isRunOnMainThread()
        val isClassRunOnUiOrMainThread = isClassRunOnMainThread || isClassRunOnUiThread

        clazz.methods.forEach { method ->
            val isMethodRunUiOrMainThread = isClassRunOnUiOrMainThread
                    || method.isRunOnUiThread(clazz)
                    || method.isRunOnMainThread(clazz)
                    || method.isSubscribeOnMainThread()

            if (isMethodRunUiOrMainThread) {
                val node = CallNode(clazz.name, method.name, method.desc)

                if (isClassRunOnMainThread) {
                    nodesRunOnMainThread += node
                }

                if (isClassRunOnUiThread) {
                    nodesRunOnUiThread += node
                }

                globalBuilder.addEdge(ROOT, node)
            }

            // construct call graph by scanning INVOKE* instructions
            method.instructions.iterator().asIterable().filterIsInstance(MethodInsnNode::class.java).forEach { invoke ->
                val to = CallNode(invoke.owner, invoke.name, invoke.desc)
                val from = CallNode(clazz.name, method.name, method.desc)

                // break circular invocation
                if (!globalBuilder.hasEdge(to, from)) {
                    globalBuilder.addEdge(from, to)
                }
            }
        }
    }

    /**
     * Rendering call graph as individual dot format
     */
    private fun dump(output: File) {
        val global = globalBuilder.build()
        val executor = Executors.newFixedThreadPool(NCPU)

        println("Generating call graphs ...")

        // Analyse global call graph and separate each chain to individual graph
        global[ROOT].map { node ->
            executor.submit {
                analyse(global, node, listOf(ROOT, node)) { chain ->
                    val builder = graphBuilders.getOrPut(node.type) {
                        Graph.Builder<CallNode>().setTitle(node.type.replace('/', '.'))
                    }
                    builder.addEdges(chain)
                }
            }
        }.forEach {
            it.get()
        }

        try {
            graphBuilders.map { (name, builder) ->
                File(output, name.separatorsToSystem() + ".dot") to builder.build()
            }.map { (dot, graph) ->
                executor.submit {
                    DotGraph.DIGRAPH.visualize(graph, dot) {
                        if (it == ROOT) graph.title else it.toPrettyString()
                    }
                }
            }.forEach {
                it.get()
            }
        } finally {
            executor.shutdown()
            executor.awaitTermination(1L, TimeUnit.HOURS)
        }
    }

    /**
     * Analyse from *node* recursively
     *
     * @param node The entry point
     * @param chain The call chain
     */
    private fun analyse(graph: Graph<CallNode>, node: CallNode, chain: List<CallNode>, action: (List<CallNode>) -> Unit) {
        if (node in whitelist) {
            return
        }

        graph[node].forEach loop@{ target ->
            // break circular invocation
            if (chain.contains(target)) {
                return@loop
            }

            val newChain = chain.plus(target)
            if (target matches blacklist) {
                action(newChain)
                return@loop
            }

            analyse(graph, target, newChain, action)
        }
    }

    /**
     * Check if this class is run on main thread
     */
    private fun ClassNode.isRunOnMainThread() = isRunOnThread(mainThreadAnnotations, classesRunOnMainThread)

    /**
     * Check if this class is run on UI thread
     */
    private fun ClassNode.isRunOnUiThread() = isRunOnThread(uiThreadAnnotations, classesRunOnUiThread)

    private fun ClassNode.isRunOnThread(annotations: Set<String>, classesRunOnThread: MutableMap<String, ClassNode>): Boolean {
        return isInvisibleAnnotationPresent(annotations) || classesRunOnThread.containsKey(name)
    }

    /**
     * Check if this method is run on main thread
     */
    private fun MethodNode.isRunOnMainThread(clazz: ClassNode) = isRunOnThread(clazz, mainThreadAnnotations, nodesRunOnMainThread)

    /**
     * Check if this method is run on UI thread
     */
    private fun MethodNode.isRunOnUiThread(clazz: ClassNode) = isRunOnThread(clazz, uiThreadAnnotations, nodesRunOnUiThread)

    private fun MethodNode.isRunOnThread(clazz: ClassNode, annotations: Set<String>, nodesRunOnThread: Set<CallNode>): Boolean {
        if (this.isInvisibleAnnotationPresent(annotations)) {
            return true
        }

        return nodesRunOnThread.any {
            it.name == this.name && it.args == this.args && hierarchy.isInheritFrom(clazz, it.type)
        }
    }

    private infix fun CallNode.matches(apis: Collection<CallNode>) = apis.contains(this) || apis.any {
        // only match type, name and args because of covariant return type is partially allowed since JDK 1.5
        // (overridden method can have different return type in sub-type)
        this.name == it.name && this.args == it.args && hierarchy.isInheritFrom(this.type, it.type)
    }
}

private val File.bootClasspath: Collection<File>
    get() = listOf(resolve("android.jar"), resolve("optional").resolve("org.apache.http.legacy.jar"))

internal fun MethodNode.isSubscribeOnMainThread(): Boolean {
    return visibleAnnotations
            ?.find { it.desc == EVENTBUS_SUBSCRIBE }
            ?.getValue<Array<String>>("threadMode")
            ?.contentEquals(arrayOf(EVENTBUS_THREAD_MODE, "MAIN")) ?: false
}

/**
 * Excludes classes with conditions:
 *
 * - class in the ignore list
 * - annotation classes
 * - class has no methods containing any *invoke* instruction
 */
private val ClassNode.isInclude: Boolean
    get() = !(EXCLUDES matches name || isAnnotation || ((isInterface || isAbstract) && methods.none { !it.isAbstract }))

private val MethodNode.isEntryPoint: Boolean
    get() = (isPublic || isProtected) && !isNative && !isStatic

private fun descriptor(name: String) = "L${name};"

private const val EVENTBUS_SUBSCRIBE = "Lorg/greenrobot/eventbus/Subscribe;"
private const val EVENTBUS_THREAD_MODE = "Lorg/greenrobot/eventbus/ThreadMode;"

private val PROPERTY_PREFIX = Build.ARTIFACT.replace('-', '.')

private val PROPERTY_BLACKLIST = "$PROPERTY_PREFIX.blacklist"

private val PROPERTY_WHITELIST = "$PROPERTY_PREFIX.whitelist"

internal val VALUE_BLACKLIST = PerformanceAnalyser::class.java.classLoader.getResource("blacklist.txt")!!.toString()

internal val VALUE_WHITELIST = PerformanceAnalyser::class.java.classLoader.getResource("whitelist.txt")!!.toString()

