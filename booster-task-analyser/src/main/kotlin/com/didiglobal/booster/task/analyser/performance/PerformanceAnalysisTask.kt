package com.didiglobal.booster.task.analyser.performance

import com.didiglobal.booster.gradle.extension
import com.didiglobal.booster.gradle.getJars
import com.didiglobal.booster.gradle.getUpstreamProjects
import com.didiglobal.booster.task.analyser.AnalysisTask
import com.didiglobal.booster.task.analyser.reportDir
import com.didiglobal.booster.transform.artifacts
import org.gradle.api.tasks.TaskAction

/**
 * Represents a task for performance analysing
 *
 * @author johnsonlee
 */
open class PerformanceAnalysisTask : AnalysisTask() {

    override fun getDescription(): String = "Analyses performance issues for Android projects"

    @TaskAction
    override fun analyse() {
        val variant = requireNotNull(this.variant)
        val classpath = project.getJars(variant) + project.getUpstreamProjects(true, variant).map {
            it.getJars(variant)
        }.flatten()

        PerformanceAnalyser(
                variant.extension.bootClasspath,
                classpath,
                variant.artifacts,
                project.properties
        ).analyse(reportDir)
    }

}