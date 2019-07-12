package com.didiglobal.booster.aapt2

import com.didiglobal.booster.aapt.Configuration
import com.didiglobal.booster.aapt2.Aapt2Container.Entry
import com.didiglobal.booster.aapt2.Aapt2Container.Header
import com.didiglobal.booster.aapt2.Aapt2Container.Metadata
import com.didiglobal.booster.aapt2.Aapt2Container.Png
import com.didiglobal.booster.aapt2.Aapt2Container.ResFile
import com.didiglobal.booster.aapt2.Aapt2Container.ResTable
import com.didiglobal.booster.aapt2.Aapt2Container.Xml
import com.didiglobal.booster.aapt2.legacy.ResourcesInternalLegacy
import java.io.File

val File.header: Header
    get() = BinaryParser(this).use { parser ->
        parser.parseHeader()
    }

val File.metadata: Metadata
    get() = BinaryParser(this).use { parser ->
        val magic = parser.readInt()
        parser.seek(0)

        return when (magic) {
            MAGIC -> {
                parser.parseHeader()
                val type = parser.readInt()
                val length = parser.readLong()

                when (type) {
                    RES_FILE -> parser.parseResFileMetadata()
                    else -> throw RuntimeException("Unsupported entry type 0x${type.toString(16)} `$absolutePath`")
                }
            }
            RES_FILE -> parser.parseLegacyMetadata()
            else -> throw Aapt2ParseException("Unrecognized file `$absolutePath`")
        }
    }

private fun BinaryParser.parseResFileMetadata(): Metadata {
    val headerSize = readInt()
    val dataSize = readLong()

    return parse {
        ResourcesInternal.CompiledFile.parseFrom(readBytes(headerSize))
    }.let {
        Metadata(it.resourceName, it.sourcePath, Configuration().apply {
            size = it.config.serializedSize
            if (size <= 0) {
                return@apply
            }

            imsi.apply {
                mcc = it.config.mcc.toShort()
                mnc = it.config.mnc.toShort()
            }
            locale.apply {
                // TODO language = ...
                // TODO country = ...
            }
            screenType.apply {
                orientation = it.config.orientationValue.toByte()
                touchscreen = it.config.touchscreenValue.toByte()
                density = it.config.density.toShort()
            }
            input.apply {
                keyboard = it.config.keyboardValue.toByte()
                navigation = it.config.navigationValue.toByte()
                flags = 0 // TODO
            }
            screenSize.apply {
                width = it.config.screenWidth.toShort()
                height = it.config.screenHeight.toShort()
            }
            version.apply {
                sdk = it.config.sdkVersion.toShort()
                minor = 0
            }
            screenConfig.apply {
                layout = it.config.layoutDirectionValue.toByte()
                uiMode = it.config.uiModeTypeValue.toByte()
                smallestWidthDp = it.config.smallestScreenWidthDp.toShort()
            }
            screenSizeDp.apply {
                width = it.config.screenWidthDp.toShort()
                height = it.config.screenHeightDp.toShort()
            }
            // TODO localScript = ...
            it.config.localeBytes.takeIf { l ->
                l.size() > 0
            }?.let { l ->
                l.copyTo(localeVariant, 0, 0, l.size())
            }
            screenConfig2.apply {
                layout = it.config.screenRoundValue.toByte()
                colorMode = (it.config.hdrValue shl 2 and it.config.wideColorGamutValue).toByte()
            }
        })
    }
}

private fun BinaryParser.parseLegacyMetadata(): Metadata {
    val entryType = readInt()
    val entryLength = readLong()
    return parse {
        ResourcesInternalLegacy.CompiledFileLegacy.parseFrom(readBytes(entryLength.toInt()))
    }.let {
        Metadata(it.resourceName, it.sourcePath, BinaryParser(it.config.data.toByteArray()).use { parser ->
            parser.parseConfiguration()
        })
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
        //seek(p + length.toInt())
    }

}

fun BinaryParser.parseConfiguration() = Configuration().apply {
    size = readInt()
    imsi.mcc = readShort()
    imsi.mnc = readShort()
    locale.language[1] = readByte()
    locale.language[0] = readByte()
    locale.country[1] = readByte()
    locale.country[0] = readByte()
    screenType.orientation = readByte()
    screenType.touchscreen = readByte()
    screenType.density = readShort()
    input.keyboard = readByte()
    input.navigation = readByte()
    input.flags = readByte()
    input.pad0 = readByte()
    screenSize.width = readShort()
    screenSize.height = readShort()
    version.sdk = readShort()
    version.minor = readShort()

    if (size >= 32) {
        screenConfig.layout = readByte()
        screenConfig.uiMode = readByte()
        screenConfig.smallestWidthDp = readShort()
    }

    // Android 3.0+
    if (size >= 36) {
        screenSizeDp.width = readShort()
        screenSizeDp.height = readShort()
    }

    // Android 5.0+
    if (size >= 48) {
        localeScript[0] = readByte()
        localeScript[1] = readByte()
        localeScript[2] = readByte()
        localeScript[3] = readByte()
        localeVariant[0] = readByte()
        localeVariant[1] = readByte()
        localeVariant[2] = readByte()
        localeVariant[3] = readByte()
        localeVariant[4] = readByte()
        localeVariant[5] = readByte()
        localeVariant[6] = readByte()
        localeVariant[7] = readByte()
    }

    // Android 6.0+
    if (size >= 52) {
        screenConfig2.layout = readByte()
        screenConfig2.colorMode = readByte()
        screenConfig2.pad2 = readShort()
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
        Resources.FileReference.Type.PNG -> parsePng(header)
        Resources.FileReference.Type.BINARY_XML -> TODO("binary XML")
        Resources.FileReference.Type.PROTO_XML -> Xml(header, Resources.XmlNode.parseFrom(readBytes(dataSize.toInt())))
        Resources.FileReference.Type.UNKNOWN -> when (header.resourcePath.substringAfter('.')) {
            "png", "9.png" -> parsePng(header)
            else -> TODO("Unknown RES_FILE `$file`")
        }
        Resources.FileReference.Type.UNRECOGNIZED -> TODO("Unrecognized resource file `${header.sourcePath}`")
    }
}

private fun BinaryParser.parsePng(header: ResourcesInternal.CompiledFile): Png {
    return Png(header, parse {
        val p = tell()
        val magic = readInt()
        if (0x474E5089 != magic) {
            throw Aapt2ParseException("Not a PNG entry `$file`")
        }
        seek(p)
        it
    })
}

val ResourcesInternal.CompiledFile.resourcePath: String
    get() {
        val src = File(this.sourcePath)
        return "${src.parentFile.name}${File.separatorChar}${src.name}"
    }

