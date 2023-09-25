package com.didiglobal.booster.task.dependency

import com.android.build.api.variant.Variant
import com.didiglobal.booster.gradle.dependencies
import com.didiglobal.booster.kotlinx.CSI_RESET
import com.didiglobal.booster.kotlinx.CSI_YELLOW
import com.didiglobal.booster.kotlinx.ifNotEmpty
import org.gradle.api.DefaultTask
import org.gradle.api.internal.artifacts.repositories.resolver.MavenUniqueSnapshotComponentIdentifier
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction

internal open class CheckSnapshot : DefaultTask() {

    @get:Internal
    lateinit var variant: Variant

    @TaskAction
    fun run() {
        variant.dependencies.filter {
            it.id.componentIdentifier is MavenUniqueSnapshotComponentIdentifier
        }.map {
            it.id.componentIdentifier as MavenUniqueSnapshotComponentIdentifier
        }.ifNotEmpty { snapshots ->
            println("$CSI_YELLOW ⚠️  ${snapshots.size} SNAPSHOT artifacts found in ${variant.name} variant:$CSI_RESET\n${snapshots.joinToString("\n") { snapshot -> "$CSI_YELLOW→  ${snapshot.displayName}$CSI_RESET" }}")
        }
    }

}
