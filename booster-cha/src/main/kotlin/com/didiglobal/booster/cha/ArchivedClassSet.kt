package com.didiglobal.booster.cha

import com.didiglobal.booster.kotlinx.asIterable
import com.didiglobal.booster.kotlinx.green
import com.didiglobal.booster.kotlinx.parallelStream
import com.didiglobal.booster.transform.asm.asClassNode
import org.objectweb.asm.tree.ClassNode
import java.io.File
import java.io.FileNotFoundException
import java.util.stream.Collectors.toMap
import java.util.zip.ZipEntry
import java.util.zip.ZipFile

private val CLASS_ENTRY_FILTER = { entry: ZipEntry -> entry.name.endsWith(".class", true) }

/**
 * @author johnsonlee
 */
internal class ArchivedClassSet(val location: File) : AbstractClassSet() {

    private val zip = ZipFile(location)

    private val classes: Map<String, ClassNode> by lazy {
        zip.entries().iterator().asIterable().parallelStream().filter(CLASS_ENTRY_FILTER).map { entry ->
            zip.getInputStream(entry).asClassNode()
        }.collect(toMap(ClassNode::name) { it })
    }

    constructor(location: String) : this(File(location).takeIf {
        it.exists()
    } ?: throw FileNotFoundException(location))

    override fun get(name: String) = this.classes[name]

    override fun contains(name: String) = this.classes.containsKey(name)

    override val size: Int
        get() = this.classes.size

    override fun isEmpty() = this.size <= 0

    override fun load(): ArchivedClassSet {
        println("Load ${green(this.classes.size)} classes from $location")
        return this
    }

    override fun iterator(): Iterator<ClassNode> = this.classes.values.iterator()

    override fun close() = zip.close()

    override fun toString(): String = this.location.canonicalPath

}