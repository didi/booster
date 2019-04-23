package com.didiglobal.booster.transform;

import org.jetbrains.annotations.NotNull;

/**
 * Represents the transform lifecycle listener
 *
 * @author johnsonlee
 */
public interface TransformListener {

    default void onPreTransform(@NotNull final TransformContext context) {
    }

    default void onPostTransform(@NotNull final TransformContext context) {
    }

}
