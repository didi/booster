package com.didiglobal.booster.gradle

import com.android.build.api.artifact.ScopedArtifact
import com.android.build.api.variant.ScopedArtifacts
import com.android.build.api.variant.Variant
import org.gradle.api.DefaultTask
import org.gradle.api.file.Directory
import org.gradle.api.file.RegularFile
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskProvider

abstract class ScopedTask : DefaultTask() {

    @get:Internal
    abstract val variant: Property<Variant>

    @get:InputFiles
    @get:PathSensitive(PathSensitivity.NONE)
    abstract val inputJars: ListProperty<RegularFile>

    @get:InputFiles
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val inputDirectories: ListProperty<Directory>

    interface CreationAction<T : ScopedTask> {

        val artifactScope: ScopedArtifacts.Scope
            get() = ScopedArtifacts.Scope.PROJECT

        val artifactType: ScopedArtifact
            get() = ScopedArtifact.CLASSES

        fun configure(task: T) {}
    }

}

/**
 * Register a task for the specified variant
 *
 * @param prefix The prefix of task name
 * @param action The task creation action
 */
inline fun <reified T : ScopedTask, reified A : ScopedTask.CreationAction<T>> Variant.registerTask(
        prefix: String,
        action: A
): TaskProvider<T> = project.tasks.register(getTaskName(prefix), T::class.java) {
    it.variant.set(this)
    action.configure(it)
}.also { taskProvider ->
    artifacts.forScope(action.artifactScope)
            .use(taskProvider)
            .toGet(action.artifactType, ScopedTask::inputJars, ScopedTask::inputDirectories)
}

/**
 * Register a task for the specified variant
 *
 * @param prefix The prefix of task name
 * @param suffix The suffix of task name
 * @param action The task creation action
 */
inline fun <reified T : ScopedTask, reified A : ScopedTask.CreationAction<T>> Variant.registerTask(
        prefix: String,
        suffix: String,
        action: A
): TaskProvider<T> = project.tasks.register(getTaskName(prefix, suffix), T::class.java) {
    it.variant.set(this)
    action.configure(it)
}.also { taskProvider ->
    artifacts.forScope(action.artifactScope)
            .use(taskProvider)
            .toGet(action.artifactType, ScopedTask::inputJars, ScopedTask::inputDirectories)
}
