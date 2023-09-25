package com.didiglobal.booster.task.compression.processed.res

import com.android.SdkConstants
import com.android.SdkConstants.DOT_PNG
import com.android.build.api.variant.Variant
import com.didiglobal.booster.BOOSTER
import com.didiglobal.booster.compression.CompressionReport
import com.didiglobal.booster.compression.CompressionResult
import com.didiglobal.booster.compression.CompressionResults
import com.didiglobal.booster.gradle.*
import com.didiglobal.booster.kotlinx.capitalized
import com.didiglobal.booster.kotlinx.file
import com.didiglobal.booster.kotlinx.search
import com.didiglobal.booster.kotlinx.touch
import com.didiglobal.booster.task.spi.VariantProcessor
import com.didiglobal.booster.transform.util.transform
import com.google.auto.service.AutoService
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry
import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction
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
class ProcessedResourcesCompressionVariantProcessor(private val project: Project) : VariantProcessor {

    override fun process(variant: Variant) {
        project.tasks.register("compress${variant.name.capitalized()}ProcessedRes", CompressProcessedRes::class.java) { task ->
            task.group = BOOSTER
            task.description = "Compress the processed resource file for ${variant.name}"
            task.variant = variant
            variant.processResTaskProvider!!.configure { processRes ->
                task.dependsOn(processRes)
                processRes.finalizedBy(task)
            }
        }
    }

}

internal abstract class CompressProcessedRes : DefaultTask() {

    @get:Internal
    lateinit var variant: Variant

    @TaskAction
    fun compress() {
        val results = CompressionResults()
        variant.compressProcessedRes(results)
        variant.generateReport(results)
    }

}

private fun Variant.compressProcessedRes(results: CompressionResults) {
    val files = processedRes.search {
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
private fun Variant.generateReport(results: CompressionResults) {
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
    val maxWith1 = table.maxOfOrNull { it.first.length } ?: 0
    val maxWith5 = table.maxOfOrNull { it.fifth.length } ?: 0
    val maxWith6 = table.maxOfOrNull { it.sixth.length } ?: 0
    val maxWith7 = table.maxOfOrNull { it.seventh.length } ?: 0
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
        logger.println(" TOTAL ${decimal(table.sumOf { it.fourth.toDouble() }).padStart(fullWith - 13)}")
    }

}

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
