package com.didiglobal.booster.transform

/**
 * Represents bytecode transformer
 *
 * @author johnsonlee
 */
interface Transformer : TransformListener {

    /**
     * Returns the transformed bytecode
     *
     * @param context
     *         The transforming context
     * @param bytecode
     *         The bytecode to be transformed
     * @return the transformed bytecode
     */
    fun transform(context: TransformContext, bytecode: ByteArray): ByteArray

}
