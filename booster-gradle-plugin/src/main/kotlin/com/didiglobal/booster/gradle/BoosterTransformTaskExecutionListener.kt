package com.didiglobal.booster.gradle

import com.android.build.gradle.internal.pipeline.TransformTask
import com.didiglobal.booster.kotlinx.getField
import com.didiglobal.booster.kotlinx.invokeMethod
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.execution.TaskExecutionListener
import org.gradle.api.tasks.TaskState

/**
 * @author neighbWang
 */
class BoosterTransformTaskExecutionListener(val project: Project) : TaskExecutionListener {
    override fun beforeExecute(task: Task) {
        task.takeIf { task.project == project && task is TransformTask }
            ?.run {
                getField(task.javaClass, "outputStream")?.let {
                    invokeMethod<Void>(it.get(task), "init")
                }
            }
    }

    override fun afterExecute(task: Task, stat: TaskState) {
    }
}