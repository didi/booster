package com.didiglobal.booster.task.compression

import com.android.SdkConstants
import com.android.build.gradle.api.BaseVariant
import com.android.build.gradle.tasks.ProcessAndroidResources
import com.didiglobal.booster.gradle.processedRes
import com.didiglobal.booster.gradle.project
import com.didiglobal.booster.gradle.scope
import com.didiglobal.booster.kotlinx.execute
import com.didiglobal.booster.task.spi.VariantProcessor
import com.didiglobal.booster.util.FileFinder
import com.google.auto.service.AutoService
import java.io.File
import java.util.zip.ZipEntry
import java.util.zip.ZipFile
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

val NO_COMPRESS = setOf(
        "jpg", "jpeg", "png", "gif",
        "wav", "mp2", "mp3", "ogg", "aac",
        "mpg", "mpeg", "mid", "midi", "smf", "jet",
        "rtttl", "imy", "xmf", "mp4", "m4a",
        "m4v", "3gp", "3gpp", "3g2", "3gpp2",
        "amr", "awb", "wma", "wmv", "webm", "mkv"
)

/**
 * Represents a variant processor for processed resources compression
 *
 * @author johnsonlee
 */
@AutoService(VariantProcessor::class)
class CompressionVariantProcessor : VariantProcessor {

    override fun process(variant: BaseVariant) {
        variant.project.tasks.withType(ProcessAndroidResources::class.java).findByName("process${variant.name.capitalize()}Resources")?.doLast {
            compressProcessedRes(variant)
        }
    }

    private fun compressProcessedRes(variant: BaseVariant) {
        FileFinder(variant.scope.processedRes) {
            it.name.startsWith(SdkConstants.FN_RES_BASE) && it.extension == SdkConstants.EXT_RES
        }.execute().forEach { ap_ ->
            ap_.repack {
                !NO_COMPRESS.contains(it.name.substringAfterLast('.'))
            }
        }
    }

}

internal fun File.repack(shouldCompress: (ZipEntry) -> Boolean) {
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
