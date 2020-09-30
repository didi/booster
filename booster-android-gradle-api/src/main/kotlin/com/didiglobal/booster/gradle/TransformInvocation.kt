package com.didiglobal.booster.gradle

import com.android.build.api.transform.TransformInvocation
import com.android.build.gradle.api.BaseVariant
import org.gradle.api.Project
import java.io.File

/**
 * Represents the booster transform for
 *
 * @author johnsonlee
 */
val TransformInvocation.project: Project
    get() = AGP.run { project }

/**
 * Returns the corresponding variant of this transform invocation
 *
 * @author johnsonlee
 */
val TransformInvocation.variant: BaseVariant
    get() = AGP.run { variant }

val TransformInvocation.bootClasspath: Collection<File>
    get() = AGP.run { bootClasspath }

/**
 * Returns the compile classpath of this transform invocation
 *
 * @author johnsonlee
 */
val TransformInvocation.compileClasspath: Collection<File>
    get() = listOf(inputs, referencedInputs).flatten().map {
        it.jarInputs + it.directoryInputs
    }.flatten().map {
        it.file
    }

/**
 * Returns the runtime classpath of this transform invocation
 *
 * @author johnsonlee
 */
val TransformInvocation.runtimeClasspath: Collection<File>
    get() = compileClasspath + bootClasspath

/**
 * Returns the application id
 */
val TransformInvocation.applicationId: String
    get() = variant.applicationId

/**
 * Returns the original application ID before any overrides from flavors
 */
val TransformInvocation.originalApplicationId: String
    get() = variant.originalApplicationId

val TransformInvocation.isDataBindingEnabled: Boolean
    get() = AGP.run { isDataBindingEnabled }
