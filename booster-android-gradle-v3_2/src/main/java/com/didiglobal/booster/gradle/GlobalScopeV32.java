package com.didiglobal.booster.gradle;

import com.android.build.gradle.internal.scope.GlobalScope;

/**
 * @author johnsonlee
 */
class GlobalScopeV32 {

    static boolean hasDynamicFeatures(final GlobalScope scope) {
        return scope.hasDynamicFeatures();
    }

}
