package com.didiglobal.booster.task.compression.processed.res

import com.android.SdkConstants
import com.android.SdkConstants.DOT_PNG
import com.android.build.gradle.api.BaseVariant
import com.didiglobal.booster.compression.CompressionReport
import com.didiglobal.booster.compression.CompressionResult
import com.didiglobal.booster.compression.CompressionResults
import com.didiglobal.booster.gradle.processResTask
import com.didiglobal.booster.gradle.processedRes
import com.didiglobal.booster.gradle.project
import com.didiglobal.booster.gradle.scope
import com.didiglobal.booster.kotlinx.file
import com.didiglobal.booster.kotlinx.touch
import com.didiglobal.booster.task.spi.VariantProcessor
import com.didiglobal.booster.transform.util.transform
import com.didiglobal.booster.util.search
import com.google.auto.service.AutoService
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry
import java.io.File
import java.text.DecimalFormat
import java.util.zip.ZipEntry
import java.util.zip.ZipFile


/**
 * Represents a variant processor for processed resources compression
 *
 * @author johnsonlee
 */
@AutoService(VariantProcessor::class)
class ProcessedResourcesCompressionVariantProcessor : VariantProcessor {

    override fun process(variant: BaseVariant) {
        val results = CompressionResults()

        variant.processResTask.doLast {
            variant.compressProcessedRes(results)
            variant.generateReport(results)
        }
    }

}

private fun BaseVariant.compressProcessedRes(results: CompressionResults) {
    val files = scope.processedRes.search {
        it.name.startsWith(SdkConstants.FN_RES_BASE) && it.extension == SdkConstants.EXT_RES
    }
    files.parallelStream().forEach { ap_ ->
        val s0 = ap_.length()
        ap_.repack {
            !NO_COMPRESS.contains(it.name.substringAfterLast('.'))
        }
        val s1 = ap_.length()
        results.add(CompressionResult(ap_, s0, s1, ap_))
    }
}

private fun File.repack(shouldCompress: (ZipEntry) -> Boolean) {
    val dest = File.createTempFile(SdkConstants.FN_RES_BASE + SdkConstants.RES_QUALIFIER_SEP, SdkConstants.DOT_RES)

    ZipFile(this).use {
        it.transform(dest, { origin: ZipEntry ->
            ZipArchiveEntry(origin).apply {
                method = if (shouldCompress(origin)) ZipEntry.DEFLATED else origin.method
            }
        })
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
private fun BaseVariant.generateReport(results: CompressionResults) {
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
    }

}

internal val NO_COMPRESS = setOf(
        "jpg", "jpeg", "png", "gif", "webp",
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
