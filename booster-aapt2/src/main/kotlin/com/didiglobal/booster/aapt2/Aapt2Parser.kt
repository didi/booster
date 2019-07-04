package com.didiglobal.booster.aapt2

import com.didiglobal.booster.aapt2.Aapt2Container.Entry
import com.didiglobal.booster.aapt2.Aapt2Container.Header
import com.didiglobal.booster.aapt2.Aapt2Container.Png
import com.didiglobal.booster.aapt2.Aapt2Container.ResFile
import com.didiglobal.booster.aapt2.Aapt2Container.ResTable
import com.didiglobal.booster.aapt2.Aapt2Container.Xml
import java.io.File

val File.header: Header
    get() = BinaryParser(this).use { parser ->
        parser.parseHeader()
    }

val File.metadata: ResourcesInternal.CompiledFile
    get() = BinaryParser(this).use { parser ->
        parser.parseHeader()
        val type = parser.readInt()
        val length = parser.readLong()

        return when (type) {
            RES_FILE -> {
                val headerSize = parser.readInt()
                val dataSize = parser.readLong()
                parser.parse {
                    ResourcesInternal.CompiledFile.parseFrom(parser.readBytes(headerSize))
                }
            }
            else -> throw RuntimeException("Unsupported entry type: 0x${type.toString(16)}")
        }
    }

val File.entries: List<Entry<*>>
    get() = BinaryParser(this).use { parser ->
        val header = parser.parseHeader()
        val entries = mutableListOf<Entry<*>>()

        (1..header.count).forEach { _ ->
            entries.add(parser.parseResEntry())
        }

        return entries
    }

fun BinaryParser.parseAapt2Container(): Aapt2Container {
    val header = parseHeader()
    val entries = mutableListOf<Entry<*>>()

    (1..header.count).forEach { _ ->
        entries.add(parseResEntry())
    }

    return Aapt2Container(header, *entries.toTypedArray())
}

fun BinaryParser.parseHeader(): Header {
    val magic = readInt()
    if (MAGIC != magic) {
        throw Aapt2ParseException("Invalid AAPT2 container magic 0x${magic.toString(16)}: `$file`")
    }

    val version = readInt()
    if (version <= 0) {
        throw Aapt2ParseException("Invalid AAPT2 container version $version: `$file`")
    }

    val count = readInt()
    if (count <= 0) {
        throw Aapt2ParseException("Empty AAPT2 container: `$file`")
    }

    return Header(magic, version, count)
}

fun BinaryParser.parseResEntry(): Entry<*> {
    val p = tell()
    val type = readInt()
    val length = readLong()

    try {
        return when (type) {
            RES_FILE -> parseResFile()
            RES_TABLE -> ResTable(parse {
                Resources.ResourceTable.parseFrom(it)
            })
            else -> TODO("Unknown type 0x`${type.toString(16)}`")
        }
    } finally {
        seek(p + length.toInt())
    }

}

private fun BinaryParser.parseResFile(): ResFile {
    val headerSize = readInt()
    val dataSize = readLong()
    val header = parse {
        ResourcesInternal.CompiledFile.parseFrom(readBytes(headerSize))
    }
    val padding = readBytes((4 - tell() % 4) % 4)

    return when (header.type) {
        Resources.FileReference.Type.PNG -> Png(header, parse {
            val p = tell()
            val magic = readInt()
            if (0x474E5089 != magic) {
                throw Aapt2ParseException("Not a PNG entry `$file`")
            }
            seek(p)
            it
        })
        Resources.FileReference.Type.BINARY_XML -> TODO("binary XML")
        Resources.FileReference.Type.PROTO_XML -> Xml(header, Resources.XmlNode.parseFrom(readBytes(dataSize.toInt())))
        Resources.FileReference.Type.UNKNOWN -> TODO("unknown RES_FILE")
        Resources.FileReference.Type.UNRECOGNIZED -> TODO("Unrecognized resource file `${header.sourcePath}`")
    }
}

val ResourcesInternal.CompiledFile.resourcePath: String
    get() {
        val src = File(this.sourcePath)
        return "${src.parentFile.name}${File.separatorChar}${src.name}"
    }

