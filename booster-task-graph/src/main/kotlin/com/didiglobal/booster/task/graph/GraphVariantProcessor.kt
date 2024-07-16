package com.didiglobal.booster.task.graph

import com.android.build.api.variant.Variant
import com.didiglobal.booster.gradle.project
import com.didiglobal.booster.gradle.registerTask
import com.didiglobal.booster.graph.Edge
import com.didiglobal.booster.graph.Graph
import com.didiglobal.booster.graph.dot.DotGraph
import com.didiglobal.booster.kotlinx.file
import com.didiglobal.booster.task.spi.VariantProcessor
import com.google.auto.service.AutoService
import io.johnsonlee.once.Once

@AutoService(VariantProcessor::class)
class GraphVariantProcessor : VariantProcessor {

    private val once = Once<Boolean>()

    override fun process(variant: Variant) {
        val project = variant.project
        project.gradle.taskGraph.whenReady {
            once {
                variant.generateTaskGraph()
            }
        }

        variant.registerTask("generate", "dependencyGraph", GenerateProjectGraph.CreationAction())
    }

}

private fun Variant.generateTaskGraph(): Boolean {
    val taskNames = project.gradle.startParameter.taskNames
    val dot = project.rootProject.buildDir.file(name, "${taskNames.joinToString("-") { it.replace(":", "") }}.dot")
    val title = "./gradlew ${taskNames.joinToString(" ")}"
    val graph = project.gradle.taskGraph.allTasks.map { task ->
        task.taskDependencies.getDependencies(task).map { dep ->
            task to dep
        }
    }.flatten().map { (dep, task) ->
        Edge(TaskNode(dep.path), TaskNode(task.path))
    }.fold(Graph.Builder<TaskNode>().setTitle(title)) { builder, edge ->
        builder.addEdge(edge)
        builder
    }.build()

    try {
        DotGraph.DIGRAPH.visualize(graph, dot)
    } catch (e: Exception) {
        project.logger.error(e.message)
    }
    return true
}

