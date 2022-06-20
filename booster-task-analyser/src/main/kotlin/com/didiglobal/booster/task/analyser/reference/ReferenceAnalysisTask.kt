package com.didiglobal.booster.task.analyser.reference

import com.android.build.gradle.api.BaseVariant
import com.didiglobal.booster.cha.ClassSet
import com.didiglobal.booster.cha.asm.AsmClassSet
import com.didiglobal.booster.cha.asm.Reference
import com.didiglobal.booster.cha.asm.ReferenceAnalyser
import com.didiglobal.booster.cha.asm.from
import com.didiglobal.booster.cha.fold
import com.didiglobal.booster.gradle.getJars
import com.didiglobal.booster.gradle.getResolvedArtifactResults
import com.didiglobal.booster.graph.Graph
import com.didiglobal.booster.graph.dot.DotGraph
import com.didiglobal.booster.graph.json.JsonGraphRender
import com.didiglobal.booster.kotlinx.NCPU
import com.didiglobal.booster.kotlinx.green
import com.didiglobal.booster.kotlinx.touch
import com.didiglobal.booster.kotlinx.yellow
import com.didiglobal.booster.task.analyser.reference.reporting.ReferencePageRenderer
import com.didiglobal.booster.task.analyser.reference.reporting.ReferenceReports
import com.didiglobal.booster.task.analyser.reference.reporting.ReferenceReportsImpl
import groovy.lang.Closure
import org.gradle.api.Action
import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.artifacts.component.ProjectComponentIdentifier
import org.gradle.api.reporting.Reporting
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.Nested
import org.gradle.api.tasks.TaskAction
import org.gradle.reporting.HtmlReportRenderer
import org.gradle.util.ClosureBackedAction
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

/**
 * Analysing the class reference for current variant to determine each class in this module is referenced by which class in which module
 *
 * @author johnsonlee
 */
open class ReferenceAnalysisTask : DefaultTask(), Reporting<ReferenceReports> {

    @get:Internal
    var variant: BaseVariant? = null

    @get:Internal
    val _reports: ReferenceReports by lazy {
        project.objects.newInstance(ReferenceReportsImpl::class.java, this)
    }

    @TaskAction
    fun analyse() {
        if ((!reports.html.isEnabled) && (!reports.dot.isEnabled) && (!reports.json.isEnabled)) {
            logger.warn("""
                Please enable reference analysis reports with following configuration:
                
                tasks.withType(${ReferenceAnalysisTask::class.java.simpleName}) {
                    reports {
                        html.enabled = true
                        json.enabled = true
                        dot.enabled = true
                    }
                }
            """.trimIndent())
            return
        }

        val upstream = project.getResolvedArtifactResults(true, variant).associate {
            it.id.componentIdentifier.displayName to when (val id = it.id.componentIdentifier) {
                is ProjectComponentIdentifier -> project.rootProject.project(id.projectPath).getClassSet(variant)
                else -> ClassSet.from(it.file)
            }
        }

        val origin = project.name to project.getJars(variant).map {
            ClassSet.from(it)
        }.fold()
        val graph = ReferenceAnalyser().analyse(origin, upstream) { klass, progress, duration ->
            project.logger.info("${green(String.format("%3d%%", progress * 100))} Analyse class ${klass.name} in ${yellow(duration.toMillis())} ms")
        }
        val executor = Executors.newFixedThreadPool(reports.size.coerceAtMost(NCPU))

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

    @Nested
    override fun getReports(): ReferenceReports = _reports

    override fun reports(closure: Closure<*>): ReferenceReports {
        return reports(ClosureBackedAction(closure))
    }

    override fun reports(configureAction: Action<in ReferenceReports>): ReferenceReports {
        configureAction.execute(_reports)
        return _reports
    }

    private fun generateDotReport(graph: Graph<Reference>) {
        if (!reports.dot.isEnabled) return

        try {
            val options = DotGraph.DotOptions(rankdir = "LR", format = "svg")
            DotGraph.DIGRAPH.visualize(graph, reports.dot.destination.touch(), options)
        } catch (e: Exception) {
            logger.error(e.message)
        }
    }

    private fun generateHtmlReport(graph: Graph<Reference>) {
        if (!reports.html.isEnabled) return
        HtmlReportRenderer().renderSinglePage(graph, ReferencePageRenderer(project, variant), reports.html.destination)
    }

    private fun generateJsonReport(graph: Graph<Reference>) {
        if (!reports.json.isEnabled) return

        val json = JsonGraphRender.render(graph) { node ->
            """{"component": "${node.component}", "class": "${node.klass}"}"""
        }.toString()
        reports.json.destination.touch().writeText(json)
    }

}

private fun Project.getClassSet(variant: BaseVariant?): AsmClassSet {
    return project.getJars(variant).map {
        ClassSet.from(it)
    }.fold()
}
