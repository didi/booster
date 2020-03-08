package com.didiglobal.booster.task.profile

import com.android.build.gradle.api.BaseVariant
import com.didiglobal.booster.transform.VariantTransformHelper
import com.didiglobal.booster.transform.asm.AsmTransformer
import org.gradle.api.internal.AbstractTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import java.io.File

/**
 * Represents a task for performance profiling
 *
 * @author johnsonlee
 */
open class ProfileTask : AbstractTask() {

    lateinit var variant: BaseVariant

    lateinit var supplier: () -> File

    @get:Input
    val variantName: String
        get() = variant.name

    @TaskAction
    fun profile() = VariantTransformHelper(variant, supplier()).transform(project.projectDir, AsmTransformer(ProfileTransformer()))

}