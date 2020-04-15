package com.didiglobal.booster.cha

import com.didiglobal.booster.kotlinx.green
import com.didiglobal.booster.kotlinx.search
import com.didiglobal.booster.transform.asm.asClassNode
import org.objectweb.asm.tree.ClassNode
import java.io.File
import java.io.FileNotFoundException
import java.util.stream.Collectors.toMap

private val CLASS_FILE_FILTER = { file: File -> file.extension.equals("class", true) }

/**
 * @author johnsonlee
 */
internal class DirectoryClassSet(val location: File) : AbstractClassSet() {

    private val classes: Map<String, ClassNode> by lazy {
        location.search(CLASS_FILE_FILTER).parallelStream()
                .map(File::asClassNode)
                .collect(toMap(ClassNode::name) { it })
    }

    constructor(location: String) : this(File(location).takeIf {
        it.exists()
    } ?: throw FileNotFoundException(location))

    override fun get(name: String) = this.classes[name]

    override fun contains(name: String) = this.classes.containsKey(name)

    override val size: Int
        get() = this.classes.size

    override fun isEmpty() = this.size <= 0

    override fun load(): DirectoryClassSet {
        println("Load ${green(this.classes.size)} classes from $location")
        return this
    }

    override fun iterator(): Iterator<ClassNode> = this.classes.values.iterator()

    override fun toString(): String = this.location.absolutePath

    override fun close() {
    }

}
