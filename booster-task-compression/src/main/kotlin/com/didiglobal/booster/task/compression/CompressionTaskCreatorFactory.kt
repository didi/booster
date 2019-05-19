package com.didiglobal.booster.task.compression

/**
 * Represents a factory of [CompressionTaskCreator]
 *
 * @author johnsonlee
 */
internal interface CompressionTaskCreatorFactory {

    fun newCompressionTaskCreator(): CompressionTaskCreator

}

