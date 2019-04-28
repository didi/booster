package com.didiglobal.booster.aapt2

import android.aapt.pb.internal.ResourcesInternal
import com.android.aapt.Resources
import java.lang.RuntimeException

import java.nio.ByteBuffer

/**
 * Represents the AAPT2 container
 */
class Aapt2Container(magic: Int = MAGIC, val version: Int, private vararg val _entries: Entry<Any>) {

    abstract class Entry<T>(val type: Int, val data: T)

    open class ResTableEntry(data: Resources.ResourceTable) : Entry<Resources.ResourceTable>(RES_TABLE, data)

    open class ResFileEntry(data: ResourcesInternal.CompiledFile) : Entry<ResourcesInternal.CompiledFile>(RES_FILE, data)

    open class PngEntry(header: ResourcesInternal.CompiledFile, val image: ByteBuffer) : ResFileEntry(header)

    open class XmlEntry(file: ResourcesInternal.CompiledFile, val xml: Resources.XmlNode): ResFileEntry(file)

    init {
        if (magic != MAGIC) {
            throw RuntimeException("Invalid aapt2 container")
        }
    }

    val entries: List<Entry<Any>>
        get() = listOf(*_entries)

}

const val MAGIC = 0x54504141

const val RES_TABLE = 0

const val RES_FILE = 1
