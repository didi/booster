package com.didiglobal.booster.compression

import com.android.SdkConstants
import com.android.build.gradle.api.BaseVariant
import com.didiglobal.booster.gradle.project
import com.didiglobal.booster.kotlinx.Octuple
import com.didiglobal.booster.kotlinx.Quadruple
import com.didiglobal.booster.kotlinx.file
import com.didiglobal.booster.kotlinx.touch
import java.io.File
import java.text.DecimalFormat
import java.util.concurrent.CopyOnWriteArrayList

/**
 *
 * Generates report with format like the following:
 *
 * reduction percentage | file path | reduced size
 */
fun CompressionResults.generateReport(variant: BaseVariant, artifact: String) {
    val base = variant.project.buildDir.toURI()
    val table = this.map {
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

    variant.project.buildDir.file("reports", artifact, variant.name, "report.txt").touch().printWriter().use { logger ->
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

/**
 * Compression Result
 *
 * 1. image file
 * 2. original file size
 * 3. current file size
 * 4. original file path
 */
typealias CompressionResult = Quadruple<File, Long, Long, File>

typealias CompressionResults = CopyOnWriteArrayList<CompressionResult>

/**
 * 1. relative path
 * 2. original size
 * 3. compressed size
 * 4. reduced size
 * 5. formatted reduced size
 * 6. reduction percentage
 * 7. original size
 * 8. original path
 */
typealias CompressionReport = Octuple<String, Long, Long, Long, String, String, String, File>

val percentage: (Number) -> String = DecimalFormat("#,##0.00'%'")::format

val decimal: (Number) -> String = DecimalFormat("#,##0")::format

fun isPng(file: File): Boolean = file.name.endsWith(SdkConstants.DOT_PNG, true)
        && (file.name.length < 6 || !file.name.regionMatches(file.name.length - 6, ".9", 0, 2, true))

fun isFlatPng(file: File): Boolean = file.name.endsWith(".png.flat", true)
        && (file.name.length < 11 || !file.name.regionMatches(file.name.length - 11, ".9", 0, 2, true))

fun isPngExceptRaw(file: File) : Boolean = isPng(file) && file.parentFile.name != "raw"

fun isFlatPngExceptRaw(file: File) : Boolean = isFlatPng(file) && !file.name.startsWith("raw_")
