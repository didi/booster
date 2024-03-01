package com.didiglobal.booster.transform.r.inline

import com.didiglobal.booster.aapt2.BinaryParser
import com.didiglobal.booster.aapt2.MAGIC
import com.didiglobal.booster.aapt2.RES_FILE
import com.didiglobal.booster.aapt2.Resources
import com.didiglobal.booster.aapt2.ResourcesInternal
import com.didiglobal.booster.kotlinx.isValidJavaIdentifier
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
                logger.error("Invalid AAPT2 container file: $canonicalPath")
                return emptySet()
            }

            val version = parser.readInt()
            if (version <= 0) {
                logger.error("Invalid AAPT2 container version: $canonicalPath")
                return emptySet()
            }

            val count = parser.readInt()
            if (count <= 0) {
                logger.warn("Empty AAPT2 container: $canonicalPath")
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
            val next = stack.pop()
            next.childList.filter(Resources.XmlNode::hasElement)
                    .map(Resources.XmlNode::getElement)
                    .let(stack::addAll)
            next.findRetainedSymbols()?.let(this::addAll)
        }
    }
}

internal fun Resources.XmlElement.findRetainedSymbols(): Collection<String>? = attributeList?.filter {
    it.name !in IGNORED_CONSTRAINT_LAYOUT_ATTRS
}?.map { attr ->
    when {
        attr.name == "constraint_referenced_ids" -> attr.value.split(PATTERN_COMMA)
        attr.name.startsWith("layout_constraint") -> attr.value.split(PATTERN_COMMA).filter {
            "parent" != it
        }.map {
            it.substringAfter('/')
        }.filter(String::isValidJavaIdentifier)
        else -> emptyList()
    }
}?.flatten()

private val logger = Logging.getLogger(RetainedSymbolCollector::class.java)

/**
 * ref: https://developer.android.com/reference/androidx/constraintlayout/widget/ConstraintLayout
 */
private val IGNORED_CONSTRAINT_LAYOUT_ATTRS = setOf(
        "layout_constraintHorizontal_bias",
        "layout_constraintVertical_bias",
        "layout_constraintCircleRadius",
        "layout_constraintCircleAngle",
        "layout_constraintDimensionRatio",
        "layout_constraintHeight_default",
        "layout_constraintHeight_min",
        "layout_constraintHeight_max",
        "layout_constraintHeight_percent",
        "layout_constraintWidth_default",
        "layout_constraintWidth_min",
        "layout_constraintWidth_max",
        "layout_constraintWidth_percent",
        "layout_constraintHorizontal_chainStyle",
        "layout_constraintVertical_chainStyle",
        "layout_constraintHorizontal_weight",
        "layout_constraintVertical_weight"
)

private val PATTERN_COMMA = Pattern.compile("\\s*,\\s*")
