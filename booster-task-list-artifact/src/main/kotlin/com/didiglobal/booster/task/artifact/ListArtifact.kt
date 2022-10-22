package com.didiglobal.booster.task.artifact

import com.android.build.gradle.api.BaseVariant
import com.didiglobal.booster.gradle.allArtifacts
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction
import java.io.File

internal open class ListArtifact : DefaultTask() {

    @get:Internal
    lateinit var variant: BaseVariant

    @TaskAction
    fun run() {
        val artifacts = this.variant.allArtifacts
        val maxTypeWidth: Int = artifacts.keys.maxOf { it.length }

        artifacts.forEach { (type, files) ->
            println("${".".repeat(maxTypeWidth - type.length + 1)}$type : ${try {
                files.files
            } catch (e: Throwable) {
                emptyList<File>()
            }}")
        }
    }

}
