package com.didiglobal.booster.compression

import com.didiglobal.booster.compression.CompressionTaskCreator

/**
 * Represents a factory of [CompressionTaskCreator]
 *
 * @author johnsonlee
 */
internal interface CompressionTaskCreatorFactory {

    fun newCompressionTaskCreator(): CompressionTaskCreator

}

