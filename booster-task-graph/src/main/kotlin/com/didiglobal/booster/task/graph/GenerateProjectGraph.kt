package com.didiglobal.booster.task.graph

import com.didiglobal.booster.gradle.ScopedTask
import com.didiglobal.booster.gradle.filterByNameOrBuildType
import com.didiglobal.booster.gradle.getUpstreamProjects
import com.didiglobal.booster.graph.Graph
import com.didiglobal.booster.graph.dot.DotGraph
import com.didiglobal.booster.kotlinx.file
import org.gradle.api.tasks.TaskAction
import java.util.Stack

abstract class GenerateProjectGraph : ScopedTask() {
    @TaskAction
    fun generate() {
        val rootProject = project.rootProject
        val filter = variant.get().filterByNameOrBuildType()
        val graph = Graph.Builder<ProjectNode>().setTitle(project.toString())
        val stack = Stack<ProjectNode>().apply {
            add(ProjectNode(project.path))
        }

        while (stack.isNotEmpty()) {
            val from = stack.pop()
            rootProject.project(from.path).getUpstreamProjects(false, filter).map {
                ProjectNode(it.path)
            }.filter { to ->
                !graph.hasEdge(from, to)
            }.takeIf(List<ProjectNode>::isNotEmpty)?.forEach { to ->
                stack.push(to)
                graph.addEdge(from, to)
            }
        }

        try {
            val dot = project.buildDir.file(variant.get().name, "dependencies.dot")
            DotGraph.DIGRAPH.visualize(graph.build(), dot, DotGraph.DotOptions(format = "svg", rankdir = "LR"))
        } catch (e: Throwable) {
            project.logger.error(e.message)
        }
    }

    class CreationAction : ScopedTask.CreationAction<GenerateProjectGraph>
}