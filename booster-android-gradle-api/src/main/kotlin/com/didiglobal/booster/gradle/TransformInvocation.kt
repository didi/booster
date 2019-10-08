package com.didiglobal.booster.gradle

import com.android.build.api.transform.TransformInvocation
import com.android.build.gradle.AppExtension
import com.android.build.gradle.BaseExtension
import com.android.build.gradle.LibraryExtension
import com.android.build.gradle.api.BaseVariant
import org.gradle.api.Project
import org.gradle.api.internal.AbstractTask
import java.io.File

/**
 * Represents the booster transform for
 *
 * @author johnsonlee
 */
val TransformInvocation.project: Project
    get() = (this.context as AbstractTask).project

/**
 * Returns the corresponding variant of this transform invocation
 *
 * @author johnsonlee
 */
val TransformInvocation.variant: BaseVariant
    get() = project.getAndroid<BaseExtension>().let { android ->
        this.context.variantName.let { variant ->
            when (android) {
                is AppExtension -> when {
                    variant.endsWith("AndroidTest") -> android.testVariants.single { it.name == variant }
                    variant.endsWith("UnitTest") -> android.unitTestVariants.single { it.name == variant }
                    else -> android.applicationVariants.single { it.name == variant }
                }
                is LibraryExtension -> android.libraryVariants.single { it.name == variant }
                else -> TODO("variant not found")
            }
        }
    }

val TransformInvocation.bootClasspath: Collection<File>
    get() = project.getAndroid<BaseExtension>().bootClasspath

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
    get() = variant.variantData.applicationId

/**
 * Returns the original application ID before any overrides from flavors
 */
val TransformInvocation.originalApplicationId: String
    get() = variant.variantData.variantConfiguration.originalApplicationId
