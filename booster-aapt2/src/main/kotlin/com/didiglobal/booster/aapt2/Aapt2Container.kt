package com.didiglobal.booster.aapt2

import com.didiglobal.booster.aapt.Configuration
import java.io.File
import java.nio.ByteBuffer

/**
 * Represents the AAPT2 container
 */
class Aapt2Container(val header: Header, private vararg val _entries: Entry<*>) {

    data class Header(val magic: Int = MAGIC, val version: Int = 1, val count: Int = 1)

    data class Metadata(val resourceName: String, val sourcePath: String, val configuration: Configuration) {

        val sourceFile = File(sourcePath)

        val resourcePath = "${sourceFile.parentFile.name}${File.separatorChar}${sourceFile.name}"

    }

    abstract class Entry<T>(val type: Int, val data: T)

    open class ResTable(data: Resources.ResourceTable) : Entry<Resources.ResourceTable>(RES_TABLE, data)

    open class ResFile(data: ResourcesInternal.CompiledFile) : Entry<ResourcesInternal.CompiledFile>(RES_FILE, data)

    open class Png(header: ResourcesInternal.CompiledFile, val image: ByteBuffer) : ResFile(header)

    open class Xml(file: ResourcesInternal.CompiledFile, val root: Resources.XmlNode): ResFile(file)

    val entries: List<Entry<*>>
        get() = listOf(*_entries)

}

const val MAGIC = 0x54504141

const val RES_TABLE = 0

const val RES_FILE = 1
