package com.didiglobal.booster.transform.asm;

import com.didiglobal.booster.transform.TransformContext;
import com.didiglobal.booster.transform.TransformListener;
import org.jetbrains.annotations.NotNull;
import org.objectweb.asm.tree.ClassNode;

/**
 * Represents class transformer
 *
 * @author johnsonlee
 */
public interface ClassTransformer extends TransformListener {

    /**
     * Transform the specified class node
     *
     * @param context
     *         The transform context
     * @param klass
     *         The class node to be transformed
     * @return The transformed class node
     */
    @NotNull
    default ClassNode transform(@NotNull final TransformContext context, @NotNull final ClassNode klass) {
        return klass;
    }

}
