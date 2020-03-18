package com.didiglobal.booster.task.analyser

import com.android.build.gradle.api.BaseVariant
import com.didiglobal.booster.gradle.extension
import com.didiglobal.booster.kotlinx.file
import com.didiglobal.booster.transform.artifacts
import org.gradle.api.internal.AbstractTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import java.io.File

/**
 * Represents a task for performance profiling
 *
 * @author johnsonlee
 */
open class AnalyserTask : AbstractTask() {

    lateinit var variant: BaseVariant

    lateinit var supplier: () -> File

    @get:Input
    val variantName: String
        get() = variant.name

    @TaskAction
    fun profile() {
        val classpath = supplier().let {
            if (it.isDirectory) {
                it.listFiles()?.toList() ?: emptyList()
            } else {
                listOf(it)
            }
        }.filter {
            it.isDirectory || it.extension.run {
                equals("class", true) || equals("jar", true)
            }
        }

        val output = project.projectDir.file("build", "reports", Build.ARTIFACT, variant.dirName)

        Analyser(variant.extension.bootClasspath, classpath, variant.artifacts, project.properties).analyse(output)
    }

}