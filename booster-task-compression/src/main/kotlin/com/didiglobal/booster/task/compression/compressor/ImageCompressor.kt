package com.didiglobal.booster.task.compression.compressor

import java.io.File

/**
 * Represents an image compressor
 *
 * @author johnsonlee
 */
interface ImageCompressor {

    /**
     * The executable of this image compressor
     */
    val executable: File?

}
