package com.didiglobal.booster.task.profile

import com.android.build.gradle.BaseExtension
import com.android.build.gradle.api.BaseVariant
import com.didiglobal.booster.gradle.getAndroid
import com.didiglobal.booster.transform.asm.AsmTransformer
import com.didiglobal.booster.transform.util.TransformHelper
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
    fun profile() {
        val android = project.getAndroid<BaseExtension>()
        val platform = android.sdkDirectory.resolve("platforms").resolve(android.compileSdkVersion)
        val transformer = AsmTransformer(ProfileTransformer())
        TransformHelper(supplier(), platform, variant.applicationId, variant.dirName).transform(project.projectDir, transformer)
    }

}