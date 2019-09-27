package com.didiglobal.booster.transform.r.inline

import com.didiglobal.booster.aapt2.BinaryParser
import com.didiglobal.booster.aapt2.MAGIC
import com.didiglobal.booster.aapt2.RES_FILE
import com.didiglobal.booster.aapt2.Resources
import com.didiglobal.booster.aapt2.ResourcesInternal
import com.didiglobal.booster.kotlinx.stackTraceAsString
import org.gradle.api.logging.Logging
import java.io.File
import java.util.Stack
import java.util.concurrent.RecursiveTask
import java.util.regex.Pattern

/**
 * Represents a collector for retained symbols collecting
 *
 * @author johnsonlee
 */
internal class RetainedSymbolCollector(private val root: File) : RecursiveTask<Collection<String>>() {

    override fun compute(): Collection<String> {
        val tasks = mutableListOf<RecursiveTask<Collection<String>>>()
        val result = mutableSetOf<String>()

        root.listFiles()?.forEach { file ->
            if (file.isDirectory) {
                RetainedSymbolCollector(file).also { task ->
                    tasks.add(task)
                }.fork()
            } else if ((file.name.startsWith("layout_") || file.name.startsWith("layout-")) && file.name.endsWith(".xml.flat")) {
                result.addAll(file.parseLayoutXml())
            }
        }

        return result + tasks.flatMap { it.join() }
    }

}

internal fun File.parseLayoutXml(): Collection<String> {
    try {
        BinaryParser(this).use { parser ->
            val magic = parser.readInt()
            if (MAGIC != magic) {
                logger.error("Invalid AAPT2 container file: $absolutePath")
                return emptySet()
            }

            val version = parser.readInt()
            if (version <= 0) {
                logger.error("Invalid AAPT2 container version: $absolutePath")
                return emptySet()
            }

            val count = parser.readInt()
            if (count <= 0) {
                logger.warn("Empty AAPT2 container: $absolutePath")
                return emptySet()
            }

            return (1..count).map {
                parser.parseResEntry()
            }.flatten().toSet()
        }
    } catch (e: Throwable) {
        logger.error(e.stackTraceAsString)
    }

    return emptySet()
}

internal fun BinaryParser.parseResEntry(): Collection<String> {
    val p = tell()
    val type = readInt()
    val length = readLong()

    if (type == RES_FILE) {
        val headerSize = readInt()
        val dataSize = readLong()
        val header = parse {
            ResourcesInternal.CompiledFile.parseFrom(readBytes(headerSize))
        }

        skip((4 - (tell() % 4)) % 4) // skip padding

        if (header.type == Resources.FileReference.Type.PROTO_XML) {
            return parse {
                Resources.XmlNode.parseFrom(readBytes(dataSize.toInt()))
            }.findAllRetainedSymbols()
        }
    }

    seek(p + length.toInt())

    return emptySet()
}

internal fun Resources.XmlNode.findAllRetainedSymbols(): Collection<String> {
    val stack = Stack<Resources.XmlElement>().apply {
        push(element)
    }

    return mutableSetOf<String>().apply {
        while (stack.isNotEmpty()) {
            stack.pop().also { element ->
                element.childList.filter {
                    it.hasElement()
                }.forEach {
                    stack.push(it.element)
                }
            }.attributeList?.forEach { attr ->
                when (attr.name) {
                    "constraint_referenced_ids" -> addAll(attr.value.split(PATTERN_COMMA))
                    else -> if (attr.name.startsWith("layout_constraint")) {
                        addAll(attr.value.split(PATTERN_COMMA).filter {
                            it != "parent"
                        }.map {
                            it.substringAfter('/')
                        })
                    }
                }
            }
        }
    }
}

private val logger = Logging.getLogger(RetainedSymbolCollector::class.java)

private val PATTERN_COMMA = Pattern.compile("\\s*,\\s*")
