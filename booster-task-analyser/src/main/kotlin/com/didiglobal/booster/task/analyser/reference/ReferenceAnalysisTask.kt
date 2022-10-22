package com.didiglobal.booster.task.analyser.reference

import com.didiglobal.booster.cha.asm.Reference
import com.didiglobal.booster.cha.asm.ReferenceAnalyser
import com.didiglobal.booster.cha.fold
import com.didiglobal.booster.gradle.getJars
import com.didiglobal.booster.gradle.getResolvedArtifactResults
import com.didiglobal.booster.graph.Graph
import com.didiglobal.booster.graph.dot.DotGraph
import com.didiglobal.booster.graph.json.JsonGraphRender
import com.didiglobal.booster.kotlinx.green
import com.didiglobal.booster.kotlinx.yellow
import com.didiglobal.booster.task.analyser.AnalysisTask
import com.didiglobal.booster.task.analyser.report
import org.gradle.api.artifacts.component.ProjectComponentIdentifier
import org.gradle.api.tasks.TaskAction
import org.gradle.reporting.HtmlReportRenderer
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

/**
 * Analysing the class reference for current variant to determine each class in this module is referenced by which class in which module
 *
 * @author johnsonlee
 */
open class ReferenceAnalysisTask : AnalysisTask() {

    override fun getDescription(): String = "Analysing class reference for Android/Java projects"

    @TaskAction
    override fun analyse() {
        val upstream = project.getResolvedArtifactResults(true, variant).associate {
            it.id.componentIdentifier.displayName to when (val id = it.id.componentIdentifier) {
                is ProjectComponentIdentifier -> project.rootProject.project(id.projectPath).getJars(variant).map { file ->
                    classSetCache[file.toURI().toURL()]
                }.fold()
                else -> classSetCache[it.file.toURI().toURL()]
            }
        }

        val origin = project.name to project.getJars(variant).map {
            classSetCache[it.toURI().toURL()]
        }.fold()
        val graph = ReferenceAnalyser().analyse(origin, upstream) { klass, progress, duration ->
            project.logger.info("${green(String.format("%3f%%", progress * 100))} Analyse class ${klass.name} in ${yellow(duration.toMillis())} ms")
        }
        val executor = Executors.newFixedThreadPool(3)

        try {
            arrayOf(::generateHtmlReport, ::generateDotReport, ::generateJsonReport).map { render ->
                executor.submit {
                    render(graph)
                }
            }.forEach {
                it.get()
            }
        } finally {
            executor.shutdown()
            executor.awaitTermination(1, TimeUnit.MINUTES)
        }
    }

    private fun generateDotReport(graph: Graph<Reference>) {
        try {
            val options = DotGraph.DotOptions(rankdir = "LR", format = "svg")
            DotGraph.DIGRAPH.visualize(graph, report("dot"), options)
        } catch (e: Exception) {
            logger.error(e.message)
        }
    }

    private fun generateHtmlReport(graph: Graph<Reference>) {
        HtmlReportRenderer().renderSinglePage(graph, ReferencePageRenderer(project, variant), report("html"))
    }

    private fun generateJsonReport(graph: Graph<Reference>) {
        val json = JsonGraphRender.render(graph) { node ->
            """{"component": "${node.component}", "class": "${node.klass}"}"""
        }.toString()
        report("json").writeText(json)
    }

}
