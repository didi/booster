package com.didiglobal.booster.task.compression

import com.android.SdkConstants
import com.android.SdkConstants.DOT_PNG
import com.android.build.gradle.api.BaseVariant
import com.android.build.gradle.tasks.ProcessAndroidResources
import com.didiglobal.booster.gradle.aapt2Enabled
import com.didiglobal.booster.gradle.mergeAssetsTask
import com.didiglobal.booster.gradle.mergeResourcesTask
import com.didiglobal.booster.gradle.mergedAssets
import com.didiglobal.booster.gradle.mergedRes
import com.didiglobal.booster.gradle.processedRes
import com.didiglobal.booster.gradle.project
import com.didiglobal.booster.gradle.scope
import com.didiglobal.booster.kotlinx.Sextuple
import com.didiglobal.booster.kotlinx.file
import com.didiglobal.booster.kotlinx.touch
import com.didiglobal.booster.task.compression.compressor.PROPERTY_PNGQUANT
import com.didiglobal.booster.task.compression.compressor.Pngquant
import com.didiglobal.booster.task.spi.VariantProcessor
import com.didiglobal.booster.util.search
import com.google.auto.service.AutoService
import java.io.File
import java.text.DecimalFormat
import java.util.concurrent.CopyOnWriteArrayList
import java.util.zip.ZipEntry
import java.util.zip.ZipFile
import java.util.zip.ZipOutputStream




/**
 * Represents a variant processor for resources compression, the task dependency graph shows as below:
 *
 * ```
 *                     +------------------+
 *                     | processResources |
 *                     +--------+---------+
 *                              |
 *               +--------------+-----------+
 *               |                          |
 *               v                          v
 * +-------------+------------+   +---------+---------+
 * |    compressResources     |   |  compressAssets   |
 * +-------------+------------+   +---------+---------+
 *               |                          |
 *               v                          v
 * +-------------+------------+   +---------+---------+
 * | removeRedundantResources |   |    mergeAssets    |
 * +-------------+------------+   +-------------------+
 *               |
 *               v
 * +-------------+------------+
 * |     mergeResources       |
 * +--------------------------+
 *
 * ```
 *
 * @author johnsonlee
 */
@AutoService(VariantProcessor::class)
class CompressionVariantProcessor : VariantProcessor {

    override fun process(variant: BaseVariant) {
        val aapt2 = variant.project.aapt2Enabled
        val pngFilter = if (aapt2) ::isFlatPng else ::isPng
        val results = CompressionResult()
        val processRes = variant.project.tasks.withType(ProcessAndroidResources::class.java).findByName("process${variant.name.capitalize()}Resources")?.doLast {
            compressProcessedRes(variant, results)
            generateReport(variant, results)
        }

        val klassRemoveRedundantFlatImages = if (aapt2) RemoveRedundantFlatImages::class else RemoveRedundantImages::class
        val reduceRedundancy = variant.project.tasks.create("remove${variant.name.capitalize()}RedundantResources", klassRemoveRedundantFlatImages.java) {
            it.outputs.upToDateWhen { false }
            it.variant = variant
            it.results = results
            it.sources = { variant.scope.mergedRes.search(pngFilter) }
        }.dependsOn(variant.mergeResourcesTask)

        val pngquant = Pngquant.find(variant.project.findProperty(PROPERTY_PNGQUANT)?.toString())
        if (pngquant.isInstalled) {
            // assets compression
            val compressAssets = variant.project.tasks.create("compress${variant.name.capitalize()}Assets", CompressImages::class.java) {
                it.outputs.upToDateWhen { false }
                it.variant = variant
                it.results = results
                it.sources = { variant.scope.mergedAssets.search(::isPng) }
                it.compressor = pngquant
            }.dependsOn(variant.mergeAssetsTask)

            // resources compression
            val klassCompressImage = if (aapt2) CompressFlatImages::class else CompressImages::class
            val compressResources = variant.project.tasks.create("compress${variant.name.capitalize()}Resources", klassCompressImage.java) {
                it.outputs.upToDateWhen { false }
                it.variant = variant
                it.results = results
                it.sources = { variant.scope.mergedRes.search(pngFilter) }
                it.compressor = pngquant
            }.dependsOn(reduceRedundancy)

            processRes?.dependsOn(compressAssets, compressResources)
        }
    }

}

private fun compressProcessedRes(variant: BaseVariant, results: CompressionResult) {
    val files = variant.scope.processedRes.search {
        it.name.startsWith(SdkConstants.FN_RES_BASE) && it.extension == SdkConstants.EXT_RES
    }
    files.parallelStream().forEach { ap_ ->
        val s0 = ap_.length()
        ap_.repack {
            !NO_COMPRESS.contains(it.name.substringAfterLast('.'))
        }
        val s1 = ap_.length()
        results.add(Triple(ap_, s0, s1))
    }
}

private fun File.repack(shouldCompress: (ZipEntry) -> Boolean) {
    val dest = File.createTempFile(SdkConstants.FN_RES_BASE + SdkConstants.RES_QUALIFIER_SEP, SdkConstants.DOT_RES)

    ZipOutputStream(dest.outputStream()).use { output ->
        ZipFile(this).use { zip ->
            zip.entries().asSequence().forEach { origin ->
                val target = ZipEntry(origin.name).apply {
                    size = origin.size
                    crc = origin.crc
                    comment = origin.comment
                    extra = origin.extra
                    method = if (shouldCompress(origin)) ZipEntry.DEFLATED else origin.method
                }

                output.putNextEntry(target)
                zip.getInputStream(origin).use {
                    it.copyTo(output)
                }
                output.closeEntry()
            }
        }
    }

    if (this.delete()) {
        if (!dest.renameTo(this)) {
            dest.copyTo(this, true)
        }
    }
}

/**
 *
 * Generates report with format like the following:
 *
 * reduction percentage | file path | reduced size
 */
private fun generateReport(variant: BaseVariant, results: CompressionResult) {
    val base = variant.project.buildDir.toURI()
    val table = results.map {
        // 1. relative path
        // 2. original size
        // 3. compressed size
        // 4. reduced size
        // 5. formatted reduced size
        // 6. reduction percentage
        Sextuple(
                base.relativize(it.first.toURI()).path,
                it.second,
                it.third,
                it.second - it.third,
                decimal(it.second - it.third),
                percentage((it.second - it.third).toDouble() * 100 / it.second)
        )
    }
    val maxWith1 = table.map { it.first.length }.max() ?: 0
    val maxWith5 = table.map { it.fifth.length }.max() ?: 0
    val maxWith6 = table.map { it.sixth.length }.max() ?: 0
    val fullWith = maxWith1 + maxWith5 + maxWith6 + 8

    variant.project.buildDir.file("reports", Build.ARTIFACT, variant.name, "report.txt").touch().printWriter().use { logger ->
        // sort by reduced size
        table.sortedByDescending { it.fourth }.forEach {
            logger.println("${it.sixth.padStart(maxWith6)} ${it.first.padEnd(maxWith1)} ${it.fifth.padStart(maxWith5)} bytes")
        }
        logger.println("-".repeat(maxWith1 + maxWith5 + maxWith6 + 8))
        logger.println(" TOTAL ${decimal(table.sumByDouble { it.fourth.toDouble() }).padStart(fullWith - 13)} bytes")
    }

}

/**
 * Compression Result
 *
 * - File path
 * - Reduced size
 * - Reduction percentage
 */
typealias CompressionResult = CopyOnWriteArrayList<Triple<File, Long, Long>>

private val NO_COMPRESS = setOf(
        "jpg", "jpeg", "png", "gif",
        "wav", "mp2", "mp3", "ogg", "aac",
        "mpg", "mpeg", "mid", "midi", "smf", "jet",
        "rtttl", "imy", "xmf", "mp4", "m4a",
        "m4v", "3gp", "3gpp", "3g2", "3gpp2",
        "amr", "awb", "wma", "wmv", "webm", "mkv"
)

internal val percentage: (Number) -> String = DecimalFormat("#,##0.00'%'")::format

internal val decimal: (Number) -> String = DecimalFormat("#,##0")::format

internal fun isPng(file: File): Boolean = file.name.endsWith(DOT_PNG, true)
        && (file.name.length < 6 || !file.name.regionMatches(file.name.length - 6, ".9", 0, 2, true))

internal fun isFlatPng(file: File): Boolean = file.name.endsWith(".png.flat", true)
        && (file.name.length < 11 || !file.name.regionMatches(file.name.length - 11, ".9", 0, 2, true))
