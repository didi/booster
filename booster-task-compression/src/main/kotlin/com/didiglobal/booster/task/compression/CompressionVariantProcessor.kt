package com.didiglobal.booster.task.compression

import com.android.SdkConstants
import com.android.build.gradle.api.BaseVariant
import com.android.build.gradle.tasks.ProcessAndroidResources
import com.didiglobal.booster.gradle.processedRes
import com.didiglobal.booster.gradle.project
import com.didiglobal.booster.gradle.scope
import com.didiglobal.booster.kotlinx.file
import com.didiglobal.booster.kotlinx.touch
import com.didiglobal.booster.task.spi.VariantProcessor
import com.didiglobal.booster.util.search
import com.google.auto.service.AutoService
import java.io.File
import java.io.PrintWriter
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
class CompressionVariantProcessor : VariantProcessor {

    private lateinit var logger: PrintWriter

    override fun process(variant: BaseVariant) {
        variant.preBuild.doFirst {
            logger = variant.project.buildDir.file("reports", Build.ARTIFACT, variant.name, "report.txt").touch().printWriter()
        }

        variant.project.tasks.withType(ProcessAndroidResources::class.java).findByName("process${variant.name.capitalize()}Resources")?.doLast {
            compressProcessedRes(variant)
        }

        variant.assemble.doLast {
            logger.close()
        }
    }

    private fun compressProcessedRes(variant: BaseVariant) {
        val files = variant.scope.processedRes.search {
            it.name.startsWith(SdkConstants.FN_RES_BASE) && it.extension == SdkConstants.EXT_RES
        }
        val maxWidth = files.map { it.name.length }.max() ?: 0
        val base = variant.project.projectDir.toURI()

        files.parallelStream().forEach { ap_ ->
            val s0 = ap_.length().toDouble()
            ap_.repack {
                !NO_COMPRESS.contains(it.name.substringAfterLast('.'))
            }
            val s1 = ap_.length().toDouble()
            logger.println("${base.relativize(ap_.toURI()).path} ${" ".repeat(maxWidth - ap_.name.length)}: ${decimal(s0 - s1)} bytes (${percentage((s0 - s1) * 100 / s0)})")
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

}

private val NO_COMPRESS = setOf(
        "jpg", "jpeg", "png", "gif",
        "wav", "mp2", "mp3", "ogg", "aac",
        "mpg", "mpeg", "mid", "midi", "smf", "jet",
        "rtttl", "imy", "xmf", "mp4", "m4a",
        "m4v", "3gp", "3gpp", "3g2", "3gpp2",
        "amr", "awb", "wma", "wmv", "webm", "mkv"
)

private val percentage: (Number) -> String = DecimalFormat("#,##0.00 '%'")::format
private val decimal: (Number) -> String = DecimalFormat("#,##0")::format
