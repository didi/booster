package com.didiglobal.booster.task.graph

import com.android.build.gradle.api.BaseVariant
import com.didiglobal.booster.cha.graph.CallGraph
import com.didiglobal.booster.cha.graph.dot.DotGraph
import com.didiglobal.booster.command.CommandService
import com.didiglobal.booster.gradle.project
import com.didiglobal.booster.kotlinx.OS
import com.didiglobal.booster.kotlinx.execute
import com.didiglobal.booster.kotlinx.file
import com.didiglobal.booster.task.spi.VariantProcessor
import com.google.auto.service.AutoService
import java.io.BufferedReader
import java.io.File
import java.util.concurrent.atomic.AtomicBoolean

private val DOT = "dot${OS.executableSuffix}"

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

            // write dot file
            dot.writeText(DotGraph.DIGRAPH.render(graph).toString())

            // convert dot to png
            CommandService.fromPath(DOT).location.file.let(::File).takeIf(File::exists)?.let {
                val cmdline = "${it.canonicalPath} -Tpng -O ${dot.canonicalPath}"
                project.logger.info(cmdline)
                cmdline.execute()
            }?.let { p ->
                p.waitFor()
                if (p.exitValue() != 0) {
                    project.logger.error(p.errorStream.bufferedReader().use(BufferedReader::readText))
                }
            } ?: project.logger.warn("Command `${DOT}` not found")
        }
    }

}