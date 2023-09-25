package com.didiglobal.booster.task.artifact

import com.android.build.api.variant.Variant
import com.didiglobal.booster.gradle.allArtifacts
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction
import java.io.File

internal open class ListArtifact : DefaultTask() {

    @get:Internal
    lateinit var variant: Variant

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
