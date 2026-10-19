package com.didiglobal.booster.transform

/**
 * Represents a transformer which delegates to other transform listeners, e.g. the ASM transformer delegates to class transformers.
 * It's used to explain the finalized [TransformScope]: a composite transformer requires [TransformScope.PROGRAM] if any of its
 * [delegates] does.
 *
 * @author johnsonlee
 */
interface CompositeTransformer : Transformer {

    /**
     * The transform listeners this transformer delegates to
     */
    val delegates: Iterable<TransformListener>

}
