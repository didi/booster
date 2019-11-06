package com.didiglobal.booster.gradle;

import com.android.build.api.transform.QualifiedContent;
import com.android.build.gradle.internal.pipeline.TransformManager;
import com.google.common.collect.ImmutableSet;

import java.util.Set;

class TransformManagerV32 {

    final static Set<? super QualifiedContent.Scope> SCOPE_FULL_WITH_FEATURES = TransformManager.SCOPE_FULL_WITH_FEATURES;

    final static Set<? super QualifiedContent.Scope> SCOPE_FULL_LIBRARY_WITH_FEATURES = new ImmutableSet.Builder<QualifiedContent.ScopeType>()
            .addAll(TransformManager.SCOPE_FEATURES)
            .add(QualifiedContent.Scope.PROJECT)
            .build();

}
