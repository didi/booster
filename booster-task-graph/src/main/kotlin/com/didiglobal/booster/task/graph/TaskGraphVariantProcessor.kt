package com.didiglobal.booster.task.graph

import com.android.build.gradle.api.BaseVariant
import com.didiglobal.booster.cha.graph.CallGraph
import com.didiglobal.booster.cha.graph.dot.DotGraph
import com.didiglobal.booster.gradle.project
import com.didiglobal.booster.kotlinx.file
import com.didiglobal.booster.task.spi.VariantProcessor
import com.google.auto.service.AutoService
import java.util.concurrent.atomic.AtomicBoolean

@AutoService(VariantProcessor::class)
class TaskGraphVariantProcessor : VariantProcessor {

    private var generating = AtomicBoolean(false)

    override fun process(variant: BaseVariant) {
        generating.compareAndSet(false, true).takeIf { it } ?: return

        val project = variant.project

        project.gradle.taskGraph.whenReady {
            val taskNames = project.gradle.startParameter.taskNames
            val dot = project.rootProject.buildDir.file("${taskNames.joinToString("-")}.dot")
            val title = "./gradlew ${taskNames.joinToString(" ")}"
            val graph = project.gradle.taskGraph.allTasks.map { task ->
                task.taskDependencies.getDependencies(task).map { dep ->
                    dep to task
                }
            }.flatten().map { (dep, task) ->
                CallGraph.Edge(TaskNode(dep.path), TaskNode(task.path))
            }.fold(CallGraph.Builder().setTitle(title)) { builder, edge ->
                builder.addEdge(edge)
                builder
            }.build()

            try {
                DotGraph.DIGRAPH.visualize(graph, dot)
            } catch (e: Exception) {
                project.logger.error(e.message)
            }
        }
    }

}