package com.didiglobal.booster.task.compression.cwebp

import org.gradle.api.tasks.TaskAction
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO

/**
 * Represents a task for image compression using cwebp
 *
 * @author johnsonlee
 */
internal open class CwebpCompressOpaqueImages : CwebpCompressImages() {

    @TaskAction
    override fun run() {
        compress(File::hasTransparency)
    }

}

internal fun File.hasTransparency() = ImageIO.read(this).let {
    !(it.colorModel.hasAlpha() && it.hasTransparency())
}

internal fun BufferedImage.hasTransparency(): Boolean {
    for (x in 0 until width step 3) {
        for (y in 0 until height step 3) {
            if (0xff != ((getRGB(x, y) shr 24) and 0xff)) {
                return true
            }
        }
    }
    return false
}

