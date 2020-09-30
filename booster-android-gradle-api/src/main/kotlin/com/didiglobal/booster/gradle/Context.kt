package com.didiglobal.booster.gradle

import com.android.build.api.transform.Context
import com.android.build.gradle.internal.pipeline.TransformTask

val Context.task: TransformTask
    get() = AGP.run { task }