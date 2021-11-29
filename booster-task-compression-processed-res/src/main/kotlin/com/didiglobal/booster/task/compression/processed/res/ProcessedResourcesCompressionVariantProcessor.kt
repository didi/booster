package com.didiglobal.booster.task.compression.processed.res

import com.android.SdkConstants
import com.android.SdkConstants.DOT_PNG
import com.android.build.gradle.api.BaseVariant
import com.didiglobal.booster.compression.CompressionReport
import com.didiglobal.booster.compression.CompressionResult
import com.didiglobal.booster.compression.CompressionResults
import com.didiglobal.booster.gradle.processResTaskProvider
import com.didiglobal.booster.gradle.processedRes
import com.didiglobal.booster.gradle.project
import com.didiglobal.booster.kotlinx.file
import com.didiglobal.booster.kotlinx.search
import com.didiglobal.booster.kotlinx.touch
import com.didiglobal.booster.task.spi.VariantProcessor
import com.didiglobal.booster.transform.util.transform
import com.google.auto.service.AutoService
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry
import pink.madis.apk.arsc.ResourceFile
import pink.madis.apk.arsc.ResourceTableChunk
import pink.madis.apk.arsc.StringPoolChunk
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.text.DecimalFormat
import java.util.zip.ZipEntry
import java.util.zip.ZipFile
import java.util.zip.ZipOutputStream

/**
 * Represents a variant processor for processed resources compression
 *
 * @author johnsonlee
 */
@AutoService(VariantProcessor::class)
class ProcessedResourcesCompressionVariantProcessor : VariantProcessor {
    private val redundantList = mutableListOf<List<ZipEntry>>()

    override fun process(variant: BaseVariant) {
        val results = CompressionResults()
        variant.processResTaskProvider?.configure {
            it.doLast {
                val deRedundantProcessedRes = variant.removeRedundantProcessedRes(redundantList)
                variant.compressProcessedRes(deRedundantProcessedRes, results)
                variant.generateReport(results, redundantList)
            }
        }
    }

}

/**
 * @return 1: origin .ap_
 *         2. .ap's unzip dir
 */
private fun BaseVariant.removeRedundantProcessedRes(redundantList: MutableList<List<ZipEntry>>): List<Pair<File, File>> {
    val files = processedRes.search {
        it.name.startsWith(SdkConstants.FN_RES_BASE) && it.extension == SdkConstants.EXT_RES
    }
    val list = mutableListOf<Pair<File, File>>()
    files.parallelStream().forEach { ap_ ->
        val unzipDir = File(ap_.parentFile, "${ap_.name}-unzip").apply {
            if (!exists()) mkdirs()
        }
        ZipFile(ap_).use {
            val groups = it.groupResourcesByCrc()
            it.unZip(unzipDir.absolutePath)
            unzipDir.removeRedundantProcessedRes(groups, redundantList)
        }
        list.add(ap_ to unzipDir)
    }
    return list
}

private fun BaseVariant.compressProcessedRes(deRedundantProcessedResFiles: List<Pair<File, File>>, results: CompressionResults) {
    deRedundantProcessedResFiles.parallelStream().forEach { (ap_, apUnzipDir) ->
        val s0 = ap_.length()
        ap_.repack(apUnzipDir) {
            !NO_COMPRESS.contains(it.name.substringAfterLast('.'))
        }
        val s1 = ap_.length()
        results.add(CompressionResult(ap_, s0, s1, ap_))
    }
}

private fun File.repack(unzipDir: File, shouldCompress: (ZipEntry) -> Boolean) {
    val destDeRedundancyAp = File.createTempFile("deRedundancy-" + SdkConstants.FN_RES_BASE + SdkConstants.RES_QUALIFIER_SEP, SdkConstants.DOT_RES)
    val dest = File.createTempFile(SdkConstants.FN_RES_BASE + SdkConstants.RES_QUALIFIER_SEP, SdkConstants.DOT_RES)

    ZipFile(unzipDir.zip(destDeRedundancyAp)).use {
        it.transform(dest, { origin: ZipEntry ->
            ZipArchiveEntry(origin).apply {
                method = if (shouldCompress(origin)) ZipEntry.DEFLATED else ZipEntry.STORED
            }
        })
    }

    if (this.delete()) {
        unzipDir.delete()
        if (!dest.renameTo(this)) {
            dest.copyTo(this, true)
        }
    }
}

/**
 * remove redundant processedRes and rewrite to arsc
 */
private fun File.removeRedundantProcessedRes(group: Map<String, MutableList<ZipEntry>>, redundantList: MutableList<List<ZipEntry>>) {
    val arscFile = File(this, ARSC_ENTRY)
    FileInputStream(arscFile).use { stream ->
        val resource = ResourceFile.fromInputStream(stream)
        group.asSequence()
                .filter {
                    it.value.size > 1
                }
                .forEach { entry ->
                    redundantList.add(entry.value)
                    val zips = entry.value
                    val firstRes = zips[0]
                    (1 until zips.size).forEach zipLoop@{ index ->
                        val repeatZipFile = zips[index]
                        File(this, repeatZipFile.name).delete()
                        resource.chunks
                                .asSequence()
                                .filter { it is ResourceTableChunk }.map { it as ResourceTableChunk }
                                .forEach { chunk ->
                                    val stringPoolChunk = chunk.stringPool
                                    val reduplicativeIndex = stringPoolChunk.indexOf(repeatZipFile.name)
                                    if (reduplicativeIndex != -1) {
                                        // re point to first res
                                        stringPoolChunk.setString(reduplicativeIndex, firstRes.name)
                                    }
                                }
                        FileOutputStream(arscFile).use {
                            BufferedOutputStream(it).use { bufferedOutputStream ->
                                bufferedOutputStream.write(resource.toByteArray())
                            }
                        }
                    }
                }
    }
}

/**
 *
 * Generates report with format like the following:
 *
 * reduction percentage | file path | reduced size
 */
private fun BaseVariant.generateReport(results: CompressionResults, redundantList: List<List<ZipEntry>>) {
    val base = project.buildDir.toURI()
    val table = results.map {
        val delta = it.second - it.third
        CompressionReport(
                base.relativize(it.first.toURI()).path,
                it.second,
                it.third,
                delta,
                if (delta == 0L) "0" else decimal(delta),
                if (delta == 0L) "0%" else percentage((delta).toDouble() * 100 / it.second),
                decimal(it.second),
                it.fourth
        )
    }
    val maxWith1 = table.map { it.first.length }.max() ?: 0
    val maxWith5 = table.map { it.fifth.length }.max() ?: 0
    val maxWith6 = table.map { it.sixth.length }.max() ?: 0
    val maxWith7 = table.map { it.seventh.length }.max() ?: 0
    val fullWith = maxWith1 + maxWith5 + maxWith6 + 8

    project.buildDir.file("reports", Build.ARTIFACT, name, "report.txt").touch().printWriter().use { logger ->
        // sort by reduced size and original size
        table.sortedWith(compareByDescending<CompressionReport> {
            it.fourth
        }.thenByDescending {
            it.second
        }).forEach {
            logger.println("${it.sixth.padStart(maxWith6)} ${it.first.padEnd(maxWith1)} ${it.fifth.padStart(maxWith5)} ${it.seventh.padStart(maxWith7)} ${it.eighth}")
        }
        logger.println("-".repeat(maxWith1 + maxWith5 + maxWith6 + 2))
        logger.println(" TOTAL ${decimal(table.sumByDouble { it.fourth.toDouble() }).padStart(fullWith - 13)}")

        redundantList.forEach {
            logger.println("\nremoved ${it.size - 1} entry duplicated with ${it.first().entryInfo}:")
            it.subList(1, it.size).forEachIndexed { index, zipEntry ->
                logger.println("${index + 1}. ${zipEntry.entryInfo}")
            }
        }
    }
}

private val ZipEntry.entryInfo: String
    get() = "Entry:[name:${name}, crc:${crc}, size:${size}]"


private fun ZipFile.groupResourcesByCrc(): Map<String, MutableList<ZipEntry>> {
    val groups = mutableMapOf<String, MutableList<ZipEntry>>()
    entries()
            .iterator()
            .forEach {
                val parentPath = it.name.substringBeforeLast('/', "")
                val key = "${parentPath}_${it.crc}"
                val list = if (groups.containsKey(key)) {
                    groups[key]
                } else {
                    groups[key] = mutableListOf()
                    groups[key]
                }
                list?.add(it)
            }
    return groups
}

private fun ZipFile.unZip(destDir: String) {
    val pathFile = File(destDir)
    if (!pathFile.exists()) {
        pathFile.mkdirs()
    }
    val zip = this
    val entries = zip.entries()
    while (entries.hasMoreElements()) {
        val entry = entries.nextElement() as ZipEntry
        val zipEntryName = entry.name
        val inputStream = zip.getInputStream(entry)
        val outPath = (destDir + File.separator + zipEntryName).replace("\\*".toRegex(), "/")
        val file = File(outPath.substring(0, outPath.lastIndexOf('/')))
        if (!file.exists()) {
            file.mkdirs()
        }
        if (File(outPath).isDirectory) {
            continue
        }
        val out = FileOutputStream(outPath)
        inputStream.use {
            it.copyTo(out)
        }
        out.close()
    }
}

private fun File.zip(dest: File): File {
    ZipOutputStream(FileOutputStream(dest)).use {
        compress(it, "")
    }
    return dest
}

private fun File.compress(zipOutputStream: ZipOutputStream, name: String) {
    val buf = ByteArray(2 * 1024)
    if (isFile) {
        zipOutputStream.putNextEntry(ZipEntry(name))
        var len: Int
        FileInputStream(this).use {
            while (it.read(buf).also { size -> len = size } != -1) {
                zipOutputStream.write(buf, 0, len)
            }
            zipOutputStream.closeEntry()
        }
    } else {
        val files = (listFiles() ?: return)
        if (files.isEmpty()) {
            return
        }
        files.forEach {
            it.compress(zipOutputStream, if (name.isEmpty()) it.name else name + "/" + it.name)
        }
    }
}

private fun StringPoolChunk.setString(index: Int, value: String) {
    @Suppress("UNCHECKED_CAST")
    try {
        val field = javaClass.getDeclaredField("strings")
        field.isAccessible = true
        val list = field.get(this) as MutableList<String>
        list[index] = value
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

private const val ARSC_ENTRY = "resources.arsc"

internal val NO_COMPRESS = setOf(
        "jpg", "jpeg", "png", "gif", "webp",
        "wav", "mp2", "mp3", "ogg", "aac",
        "mpg", "mpeg", "mid", "midi", "smf", "jet",
        "rtttl", "imy", "xmf", "mp4", "m4a",
        "m4v", "3gp", "3gpp", "3g2", "3gpp2",
        "amr", "awb", "wma", "wmv", "webm", "mkv", "arsc"
)

internal val percentage: (Number) -> String = DecimalFormat("#,##0.00'%'")::format

internal val decimal: (Number) -> String = DecimalFormat("#,##0")::format

internal fun isPng(file: File): Boolean = file.name.endsWith(DOT_PNG, true)
        && (file.name.length < 6 || !file.name.regionMatches(file.name.length - 6, ".9", 0, 2, true))

internal fun isFlatPng(file: File): Boolean = file.name.endsWith(".png.flat", true)
        && (file.name.length < 11 || !file.name.regionMatches(file.name.length - 11, ".9", 0, 2, true))
