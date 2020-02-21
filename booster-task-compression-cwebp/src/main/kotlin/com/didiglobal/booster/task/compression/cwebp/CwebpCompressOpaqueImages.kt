package com.didiglobal.booster.task.compression.cwebp

import org.gradle.api.tasks.CacheableTask
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.IIOException
import javax.imageio.ImageIO

/**
 * Represents a task for image compression using cwebp
 *
 * @author johnsonlee
 */
@CacheableTask
internal open class CwebpCompressOpaqueImages : CwebpCompressImages() {

    override fun compress(filter: (File) -> Boolean) = super.compress { true }

}

internal fun File.hasNotAlpha() = !hasAlpha()

internal fun File.hasAlpha() = try {
    ImageIO.read(this).let {
        it.colorModel.hasAlpha() && it.hasAlpha()
    }
} catch (e: IIOException) {
    throw IIOException("${e.message}: ${this.absolutePath}", e.cause)
}

internal fun BufferedImage.hasAlpha(): Boolean {
    for (x in 0 until width step 3) {
        for (y in 0 until height step 3) {
            if (0xff != ((getRGB(x, y) shr 24) and 0xff)) {
                return true
            }
        }
    }
    return false
}

