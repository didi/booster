package com.didiglobal.booster.transform.lint

import com.didiglobal.booster.kotlinx.MAGENTA
import com.didiglobal.booster.kotlinx.RESET
import com.didiglobal.booster.kotlinx.asIterable
import com.didiglobal.booster.kotlinx.file
import com.didiglobal.booster.transform.ArtifactManager
import com.didiglobal.booster.transform.TransformContext
import com.didiglobal.booster.transform.asm.ClassTransformer
import com.didiglobal.booster.transform.lint.graph.CallGraph
import com.didiglobal.booster.transform.lint.graph.CallGraph.Node
import com.didiglobal.booster.util.ComponentHandler
import com.google.auto.service.AutoService
import org.objectweb.asm.tree.ClassNode
import org.objectweb.asm.tree.MethodInsnNode
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

    override fun transform(context: TransformContext, klass: ClassNode): ClassNode {
        klass.methods.forEach { method ->
            method.instructions.iterator().asIterable().filterIsInstance(MethodInsnNode::class.java).forEach { invoke ->
                val from = CallGraph.Node(klass.name, method.name, method.desc)
                val to = CallGraph.Node(invoke.owner, invoke.name, invoke.desc)
                // break recursive invocation
                if (!builder.hasEdge(to, from)) {
                    builder.addEdge(from, to)
                }
            }
        }
        return klass
    }

    override fun onPostTransform(context: TransformContext) {
        val factory = SAXParserFactory.newInstance()
        val parser = factory.newSAXParser()
        context.artifacts.get(ArtifactManager.MERGED_MANIFESTS).map {
            val manifest = it.file("AndroidManifest.xml")
            val graph = builder.build()
            val apis = if (context.hasProperty(PROPERTY_LINT_APIS)) context.lintApis else LINT_APIS

            ComponentHandler().let { handler ->
                parser.parse(manifest, handler)
                graph.analyse(handler.applications, APPLICATION_ENTRY_POINTS, apis)
                graph.analyse(handler.activities, ACTIVITY_ENTRY_POINTS, apis)
                graph.analyse(handler.services, SERVICE_ENTRY_POINTS, apis)
            }
        }
    }

}

private val TransformContext.lintApis: Set<Node>
    get() {
        val uri = URI(this.getProperty(PROPERTY_LINT_APIS))
        val url = if (uri.isAbsolute) uri.toURL() else File(uri).toURI().toURL()

        url.openStream().bufferedReader().use {
            return it.lines().filter(String::isNotBlank).map { line ->
                Node.from(line.trim())
            }.collect(Collectors.toSet())
        }
    }

private fun CallGraph.analyse(components: Set<String>, entryPoints: Set<EntryPoint>, apis: Set<Node>) {
    components.map {
        it.replace('.', '/')
    }.forEach { component ->
        entryPoints.map {
            Node(component, it.name, it.desc)
        }.forEach {
            analyse(it, listOf(it), apis)
        }
    }
}

private fun CallGraph.analyse(node: Node, parent: List<Node>, apis: Set<Node>) {
    parent.last()
    this.edges[node]?.forEach {
        if (parent.contains(it)) {
            return
        }

        val paths = parent.plus(it)
        if (apis.contains(it)) {
            println(" ⚠️  $MAGENTA${paths.joinToString("$RESET -> $MAGENTA")} $RESET")
            return
        }
        analyse(it, paths, apis)
    }
}

private val PROPERTY_LINT_APIS = "${Build.ARTIFACT.replace('-', '.')}.apis"

internal val LINT_APIS = setOf(
        Node("java/lang/Object", "wait", "()V"),
        Node("java/lang/Object", "wait", "(J)V"),
        Node("java/lang/Object", "wait", "(JI)V"),
        Node("java/lang/ClassLoader", "getResource", "(Ljava/lang/String;)Ljava/net/URL;"),
        Node("java/lang/ClassLoader", "getResources", "(Ljava/lang/String;)Ljava/util/Enumeration;"),
        Node("java/lang/ClassLoader", "getResourceAsStream", "(Ljava/lang/String;)Ljava/io/InputStream;"),
        Node("java/lang/ClassLoader", "getSystemResource", "(Ljava/lang/String;)Ljava/net/URL;"),
        Node("java/lang/ClassLoader", "getSystemResources", "(Ljava/lang/String;)Ljava/util/Enumeration;"),
        Node("java/lang/ClassLoader", "getSystemResourceAsStream", "(Ljava/lang/String;)Ljava/io/InputStream;"),
        Node("java/io/InputStream", "read", "()I"),
        Node("java/io/InputStream", "read", "([B)I"),
        Node("java/io/InputStream", "read", "([BII)I"),
        Node("java/io/BufferedInputStream", "read", "()I"),
        Node("java/io/BufferedInputStream", "read", "([BII)I"),
        Node("java/io/OutputStream", "write", "(I)V"),
        Node("java/io/OutputStream", "write", "([B)V"),
        Node("java/io/OutputStream", "write", "([BII)V"),
        Node("java/io/OutputStream", "flush", "()V"),
        Node("java/io/BufferedOutputStream", "write", "(I)V"),
        Node("java/io/BufferedOutputStream", "write", "([BII)V"),
        Node("java/io/BufferedOutputStream", "flush", "()V"),
        Node("java/io/Reader", "read", "()I"),
        Node("java/io/Reader", "read", "([C)I"),
        Node("java/io/Reader", "read", "([CII)I"),
        Node("java/io/Reader", "read", "(Ljava/nio/CharBuffer;)I"),
        Node("java/io/Writer", "append", "(C)Ljava/io/Writer;"),
        Node("java/io/Writer", "append", "(Ljava/lang/CharSequence;)Ljava/io/Writer;"),
        Node("java/io/Writer", "append", "(Ljava/lang/CharSequence;II)Ljava/io/Writer;"),
        Node("java/io/Writer", "flush", "()V"),
        Node("java/io/Writer", "write", "(I)V"),
        Node("java/io/Writer", "write", "(Ljava/lang/String;)V"),
        Node("java/io/Writer", "write", "(Ljava/lang/String;II)V"),
        Node("java/io/Writer", "write", "([C)V"),
        Node("java/io/Writer", "write", "([CII)V"),
        Node("java/util/ServiceLoader", "load", "(Ljava/lang/Class;)Ljava/util/ServiceLoader;"),
        Node("java/util/ServiceLoader", "load", "(Ljava/lang/Class;Ljava/lang/ClassLoader;)Ljava/util/ServiceLoader;"),
        Node("java/util/zip/ZipFile", "<init>", "(Ljava/lang/String;)"),
        Node("java/util/zip/ZipFile", "getInputStream", "(Ljava/util/zip/ZipEntry;)"),
        Node("java/util/jar/JarFile", "<init>", "(Ljava/lang/String;)"),
        Node("java/util/jar/JarFile", "getInputStream", "(Ljava/util/jar/JarEntry;)"),
        Node("android/content/Context", "getSharedPreferences", "Ljava/lang/String;I)Landroid/content/SharedPreferences;"),
        Node("android/content/SharedPreferences\$Editor", "apply", "()V"),
        Node("android/content/SharedPreferences\$Editor", "commit", "()B"),
        Node("android/content/res/AssetManager", "list", "(Ljava/lang/String;)[Ljava/lang/String;"),
        Node("android/content/res/AssetManager", "open", "(Ljava/lang/String;)Ljava/io/InputStream;"),
        Node("android/content/res/AssetManager", "open", "(Ljava/lang/String;I)Ljava/io/InputStream;"),
        Node("android/content/res/AssetManager", "openFd", "(Ljava/lang/String;)Landroid/content/res/AssetFileDescriptor;"),
        Node("android/content/res/AssetManager", "openNonAssetFd", "(Ljava/lang/String;)Landroid/content/res/AssetFileDescriptor;"),
        Node("android/content/res/AssetManager", "openNonAssetFd", "(ILjava/lang/String;)Landroid/content/res/AssetFileDescriptor;"),
        Node("android/content/res/AssetManager", "openXmlResourceParser", "(Ljava/lang/String;)Landroid/content/res/XmlResourceParser;"),
        Node("android/content/res/AssetManager", "openXmlResourceParser", "(ILjava/lang/String;)Landroid/content/res/XmlResourceParser;"),
        Node("android/database/sqlite/SQLiteDatabase", "beginTransaction", "()V"),
        Node("android/database/sqlite/SQLiteDatabase", "beginTransactionNonExclusive", "()V"),
        Node("android/database/sqlite/SQLiteDatabase", "beginTransactionWithListener", "(Landroid/database/sqlite/SQLiteTransactionListener;)V"),
        Node("android/database/sqlite/SQLiteDatabase", "delete", "(Ljava/lang/String;Ljava/lang/String;[Ljava/lang/String;)V"),
        Node("android/database/sqlite/SQLiteDatabase", "deleteDatabase", "(Ljava/io/File;)Z"),
        Node("android/database/sqlite/SQLiteDatabase", "endTransaction", "()V"),
        Node("android/database/sqlite/SQLiteDatabase", "execSQL", "(Ljava/lang/String;)V"),
        Node("android/database/sqlite/SQLiteDatabase", "execSQL", "(Ljava/lang/String;[Ljava/lang/Object;)V"),
        Node("android/database/sqlite/SQLiteDatabase", "insert", "(Ljava/lang/String;Ljava/lang/String;Landroid/content/ContentValues;)J"),
        Node("android/database/sqlite/SQLiteDatabase", "insertOrThrow", "(Ljava/lang/String;Ljava/lang/String;Landroid/content/ContentValues;)J"),
        Node("android/database/sqlite/SQLiteDatabase", "insertWithOnConflict", "(Ljava/lang/String;Ljava/lang/String;Landroid/content/ContentValues;I)J"),
        Node("android/database/sqlite/SQLiteDatabase", "query", "(ZLjava/lang/String;[java/lang/String;Ljava/lang/String;[Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)Landroid/database/Cursor;"),
        Node("android/database/sqlite/SQLiteDatabase", "query", "(Ljava/lang/String;[java/lang/String;Ljava/lang/String;[Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)Landroid/database/Cursor;"),
        Node("android/database/sqlite/SQLiteDatabase", "query", "(ZLjava/lang/String;[java/lang/String;Ljava/lang/String;[Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Landroid/os/CancellationSignal;)Landroid/database/Cursor;"),
        Node("android/database/sqlite/SQLiteDatabase", "query", "(Ljava/lang/String;[java/lang/String;Ljava/lang/String;[Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)Landroid/database/Cursor;"),
        Node("android/database/sqlite/SQLiteDatabase", "queryWithFactory", "(Landroid/database/sqlite/SQLiteDatabase\$CursorFactory;ZLjava/lang/String;[java/lang/String;Ljava/lang/String;[Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Landroid/os/CancellationSignal;)Landroid/database/Cursor;"),
        Node("android/database/sqlite/SQLiteDatabase", "queryWithFactory", "(Landroid/database/sqlite/SQLiteDatabase\$CursorFactory;ZLjava/lang/String;[java/lang/String;Ljava/lang/String;[Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)Landroid/database/Cursor;"),
        Node("android/database/sqlite/SQLiteDatabase", "rawQuery", "(Ljava/lang/String;[Ljava/lang/String;Landroid/os/CancellationSignal;)Landroid/database/Cursor;"),
        Node("android/database/sqlite/SQLiteDatabase", "rawQuery", "(Ljava/lang/String;[Ljava/lang/String;)Landroid/database/Cursor;"),
        Node("android/database/sqlite/SQLiteDatabase", "rawQueryWithFactory", "(Landroid/database/sqlite/SQLiteDatabase\$CursorFactory;Ljava/lang/String;[java/lang/String;Ljava/lang/String;Landroid/os/CancellationSignal;)Landroid/database/Cursor;"),
        Node("android/database/sqlite/SQLiteDatabase", "rawQueryWithFactory", "(Landroid/database/sqlite/SQLiteDatabase\$CursorFactory;Ljava/lang/String;[java/lang/String;Ljava/lang/String;)Landroid/database/Cursor;"),
        Node("android/database/sqlite/SQLiteDatabase", "replace", "(Ljava/lang/String;Ljava/lang/String;Landroid/content/ContentValues;)J"),
        Node("android/database/sqlite/SQLiteDatabase", "replaceOrThrow", "(Ljava/lang/String;Ljava/lang/String;Landroid/content/ContentValues;)J"),
        Node("android/database/sqlite/SQLiteDatabase", "update", "(Ljava/lang/String;Ljava/lang/String;Landroid/content/ContentValues;)J"),
        Node("android/database/sqlite/SQLiteDatabase", "updateWithOnConflict", "(Ljava/lang/String;Landroid/content/ContentValues;Ljava/lang/String;[Ljava/lang/String;I)J")
)
