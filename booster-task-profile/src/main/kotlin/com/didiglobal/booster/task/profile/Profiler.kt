package com.didiglobal.booster.task.profile

import com.didiglobal.booster.aapt2.metadata
import com.didiglobal.booster.kotlinx.NCPU
import com.didiglobal.booster.kotlinx.asIterable
import com.didiglobal.booster.kotlinx.file
import com.didiglobal.booster.kotlinx.green
import com.didiglobal.booster.kotlinx.red
import com.didiglobal.booster.kotlinx.search
import com.didiglobal.booster.kotlinx.separatorsToSystem
import com.didiglobal.booster.kotlinx.touch
import com.didiglobal.booster.kotlinx.yellow
import com.didiglobal.booster.task.profile.cha.ClassHierarchy
import com.didiglobal.booster.task.profile.cha.ClassSet
import com.didiglobal.booster.task.profile.cha.fold
import com.didiglobal.booster.task.profile.dot.GraphType
import com.didiglobal.booster.task.profile.graph.CallGraph
import com.didiglobal.booster.task.profile.graph.toEdges
import com.didiglobal.booster.transform.ArtifactManager
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
import com.intellij.psi.CommonClassNames.JAVA_LANG_OBJECT
import org.objectweb.asm.tree.ClassNode
import org.objectweb.asm.tree.MethodInsnNode
import org.objectweb.asm.tree.MethodNode
import java.io.File
import java.net.URL
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArraySet
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.Future
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger
import javax.xml.parsers.SAXParserFactory
import kotlin.streams.toList

/**
 * @author johnsonlee
 */
class Profiler(val providedClasspath: Collection<File>, val compileClasspath: Collection<File>, val artifacts: ArtifactManager) {

    private val providedClasses = providedClasspath.map(ClassSet.Companion::from).fold()

    private val compileClasses = compileClasspath.map(ClassSet.Companion::from).fold()

    private val classes = providedClasses + compileClasses

    private val hierarchy = ClassHierarchy(classes)

    /**
     * The global call graph of whole project
     */
    private val globalBuilder = CallGraph.Builder()

    /**
     * The call graph of each class
     */
    private val graphBuilders = mutableMapOf<String, CallGraph.Builder>()

    private val classesRunOnUiThread = ConcurrentHashMap<String, ClassNode>()
    private val classesRunOnMainThread = ConcurrentHashMap<String, ClassNode>()

    private val nodesRunOnUiThread = CopyOnWriteArraySet(PLATFORM_METHODS_RUN_ON_UI_THREAD)
    private val nodesRunOnMainThread = CopyOnWriteArraySet(PLATFORM_METHODS_RUN_ON_MAIN_THREAD)

    private val mainThreadAnnotations: Set<String> by lazy {
        MAIN_THREAD_ANNOTATIONS.filter(classes::contains).map(::descriptor).toSet()
    }

    private val uiThreadAnnotations: Set<String> by lazy {
        UI_THREAD_ANNOTATIONS.filter(classes::contains).map(::descriptor).toSet()
    }

    constructor(platform: File, compileClasspath: Collection<File>, artifacts: ArtifactManager)
            : this(platform.bootClasspath, compileClasspath, artifacts)

    fun profile(output: File) {
        this.classes.load().use {
            this.loadEntryPoints()
            this.analyse()
            this.dump(output)
            this.hierarchy.unresolvedClasses.forEach {
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

        // Attach component entry points to graph ROOT
        return mapOf(
                "android/app/Application" to handler.applications,
                "android/app/Activity" to handler.activities,
                "android/app/Service" to handler.services,
                "android/content/BroadcastReceiver" to handler.receivers,
                "android/content/ContentProvider" to handler.providers
        ).entries.map {
            executor.submit {
                val name = it.key
                val className = name.replace('/', '.')
                val components = it.value
                val clazz = this.hierarchy[name] ?: throw ClassNotFoundException(className)

                classesRunOnMainThread[name] = clazz
                println("Loading main thread entry points from $className ...")

                clazz.methods.filter { m ->
                    (m.isPublic || m.isProtected) && !m.isNative && !m.isStatic
                }.map { m ->
                    components.map { cn ->
                        CallGraph.Node(cn.replace('.', '/'), m.name, m.desc)
                    }
                }.flatten().forEach { node ->
                    this.hierarchy[node.type]?.apply {
                        classesRunOnMainThread[node.type] = this
                    }
                    this.globalBuilder.addEdge(CallGraph.ROOT, node)
                    this.nodesRunOnMainThread += node
                }
            }
        }
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
                val xml = header.sourceFile

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

                        hierarchy.getSuperClasses(clazz).filter {
                            it.name != JAVA_LANG_OBJECT
                        }.forEach {
                            classesRunOnUiThread[it.name] = it
                        }

                        clazz.methods.filter {
                            (it.isPublic || it.isProtected) && !it.isNative
                        }.map { m ->
                            CallGraph.Node(clazz.name, m.name, m.desc).also {
                                globalBuilder.addEdge(CallGraph.ROOT, it)
                            }
                        }
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
                val node = CallGraph.Node(clazz.name, method.name, method.desc)

                if (isClassRunOnMainThread) {
                    nodesRunOnMainThread += node
                }

                if (isClassRunOnUiThread) {
                    nodesRunOnUiThread += node
                }

                globalBuilder.addEdge(CallGraph.ROOT, node)
            }

            // construct call graph by scanning INVOKE* instructions
            method.instructions.iterator().asIterable().filterIsInstance(MethodInsnNode::class.java).forEach { invoke ->
                val to = CallGraph.Node(invoke.owner, invoke.name, invoke.desc)
                val from = CallGraph.Node(clazz.name, method.name, method.desc)

                // break circular invocation
                if (!globalBuilder.hasEdge(to, from)) {
                    globalBuilder.addEdge(from, to)
                }
            }
        }
    }

    private fun dump(output: File) {
        val graph = globalBuilder.build()
        val blacklist = URL(VALUE_BLACKLIST).openStream().bufferedReader().use {
            it.readLines().filter(String::isNotBlank).map(CallGraph.Node.Companion::valueOf).toSet()
        }
        val whitelist = URL(VALUE_WHITELIST).openStream().bufferedReader().use {
            it.readLines().filter(String::isNotBlank).map(CallGraph.Node.Companion::valueOf).toSet()
        }

        // Analyse global call graph and separate each chain to individual graph
        graph[CallGraph.ROOT].forEach { node ->
            analyse(graph, node, listOf(CallGraph.ROOT, node), blacklist, whitelist) { chain ->
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
            Pair(File(output, it.key.separatorsToSystem() + ".dot"), it.value.build())
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
     * @param node The entry point
     * @param chain The call chain
     * @param blacklist The api black list
     */
    private fun analyse(graph: CallGraph, node: CallGraph.Node, chain: List<CallGraph.Node>, blacklist: Set<CallGraph.Node>, whitelist: Set<CallGraph.Node>, action: (List<CallGraph.Node>) -> Unit) {
        graph[node].forEach loop@{ target ->
            // break circular invocation
            if (chain.contains(target) || chain.intersect(blacklist).isNotEmpty()) {
                return@loop
            }

            val newChain = chain.plus(target)
            if (isHit(blacklist, target)) {
                action(newChain)
                return@loop
            }

            analyse(graph, target, newChain, blacklist, whitelist, action)
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

    private fun MethodNode.isRunOnThread(clazz: ClassNode, annotations: Set<String>, nodesRunOnThread: Set<CallGraph.Node>): Boolean {
        if (this.isInvisibleAnnotationPresent(annotations)) {
            return true
        }

        return nodesRunOnThread.filter {
            it.name == this.name && it.desc == this.desc
        }.any {
            hierarchy.isInheritFrom(clazz, it.type)
        }
    }

    private fun isHit(apis: Set<CallGraph.Node>, target: CallGraph.Node) = apis.contains(target) || apis.any {
        target.name == it.name && target.desc == it.desc && hierarchy.isInheritFrom(target.type, it.type)
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

private fun descriptor(name: String) = "L${name};"

private const val EVENTBUS_SUBSCRIBE = "Lorg/greenrobot/eventbus/Subscribe;"
private const val EVENTBUS_THREAD_MODE = "Lorg/greenrobot/eventbus/ThreadMode;"

private val PROPERTY_PREFIX = Build.ARTIFACT.replace('-', '.')

private val PROPERTY_BLACKLIST = "$PROPERTY_PREFIX.blacklist"

private val PROPERTY_WHITELIST = "$PROPERTY_PREFIX.whitelist"

internal val VALUE_BLACKLIST = Profiler::class.java.classLoader.getResource("blacklist.txt")!!.toString()

internal val VALUE_WHITELIST = Profiler::class.java.classLoader.getResource("whitelist.txt")!!.toString()

