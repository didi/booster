package com.didiglobal.booster.task.artifact

import com.android.build.gradle.api.BaseVariant
import com.didiglobal.booster.gradle.allArtifacts
import com.didiglobal.booster.gradle.scope
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

internal open class ListArtifact : DefaultTask() {

    lateinit var variant: BaseVariant

    @TaskAction
    fun run() {
        val artifacts = this.variant.scope.allArtifacts
        val maxTypeWidth: Int = artifacts.keys.map { it.length }.max()!!

        artifacts.forEach { type, files ->
            println("${".".repeat(maxTypeWidth - type.length + 1)}$type : $files")
        }
    }

}
