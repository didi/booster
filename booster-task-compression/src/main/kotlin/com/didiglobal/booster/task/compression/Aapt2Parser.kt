package com.didiglobal.booster.task.compression

import android.aapt.pb.internal.ResourcesInternal
import com.didiglobal.booster.aapt2.BinaryParser
import com.didiglobal.booster.aapt2.MAGIC
import com.didiglobal.booster.aapt2.RES_FILE
import org.gradle.api.logging.Logging
import java.io.File

private val logger = Logging.getLogger(Build.ARTIFACT)

internal val File.metadata: ResourcesInternal.CompiledFile?
    get() = try {
        BinaryParser(this).use { parser ->
            val magic = parser.readInt()
            if (MAGIC != magic) {
                logger.error("Invalid AAPT2 container file: `${this.absolutePath}`")
                return null
            }

            val version = parser.readInt()
            if (version <= 0) {
                logger.error("Invalid AAPT2 container file: `${this.absolutePath}`")
                return null
            }

            val count = parser.readInt()
            if (count <= 0) {
                logger.warn("Empty AAPT2 container `${this.absolutePath}`")
                return null
            }

            return parser.parseResEntry()
        }
    } catch (t: Throwable) {
        logger.error("Parse `${this.absolutePath}` failed", t)
        null
    }

@Suppress("UNUSED_VARIABLE")
private fun BinaryParser.parseResEntry(): ResourcesInternal.CompiledFile? {
    val p = tell()
    val type = readInt()
    val length = readLong()

    try {
        if (type == RES_FILE) {
            val headerSize = readInt()
            val dataSize = readLong()
            return parse { ResourcesInternal.CompiledFile.parseFrom(readBytes(headerSize)) }
        }
    } finally {
        seek(p + length.toInt())
    }

    return null
}

internal val ResourcesInternal.CompiledFile.resourcePath: String
    get() {
        val src = File(this.sourcePath)
        return "${src.parentFile.name}${File.separatorChar}${src.name}"
    }

