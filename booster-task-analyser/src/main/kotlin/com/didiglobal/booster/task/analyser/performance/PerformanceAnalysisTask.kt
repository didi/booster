package com.didiglobal.booster.task.analyser.performance

import com.android.build.gradle.api.BaseVariant
import com.didiglobal.booster.gradle.extension
import com.didiglobal.booster.kotlinx.file
import com.didiglobal.booster.task.analyser.Build
import com.didiglobal.booster.task.analyser.performance.reporting.PerformanceReports
import com.didiglobal.booster.task.analyser.performance.reporting.PerformanceReportsImpl
import com.didiglobal.booster.transform.artifacts
import groovy.lang.Closure
import org.gradle.api.Action
import org.gradle.api.DefaultTask
import org.gradle.api.reporting.Reporting
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction
import org.gradle.util.ClosureBackedAction
import java.io.File

/**
 * Represents a task for performance analysing
 *
 * @author johnsonlee
 */
open class PerformanceAnalysisTask : DefaultTask(), Reporting<PerformanceReports> {

    @get:Internal
    lateinit var variant: BaseVariant

    @get:Internal
    lateinit var supplier: () -> File

    @get:Input
    val variantName: String
        get() = variant.name

    @get:Internal
    val _reports: PerformanceReports by lazy {
        project.objects.newInstance(PerformanceReportsImpl::class.java, this)
    }

    override fun getReports(): PerformanceReports = _reports

    override fun reports(closure: Closure<*>): PerformanceReports {
        return reports(ClosureBackedAction(closure))
    }

    override fun reports(configureAction: Action<in PerformanceReports>): PerformanceReports {
        configureAction.execute(_reports)
        return _reports
    }

    @TaskAction
    fun analyse() {
        if ((!reports.html.isEnabled) && (!reports.dot.isEnabled) && (!reports.json.isEnabled)) {
            logger.warn("""
                Please enable reference analysis reports with following configuration:
                
                tasks.withType(${PerformanceAnalysisTask::class.java.simpleName}) {
                    reports {
                        html.enabled = true
                        json.enabled = true
                        dot.enabled = true
                    }
                }
            """.trimIndent())
            return
        }

        val classpath = supplier().let {
            if (it.isDirectory) {
                it.listFiles()?.toList() ?: emptyList()
            } else {
                listOf(it)
            }
        }.filter {
            it.isDirectory || it.extension.run {
                equals("class", true) || equals("jar", true)
            }
        }

        val output = project.projectDir.file("build", "reports", Build.ARTIFACT, variant.dirName)

        PerformanceAnalyser(variant.extension.bootClasspath, classpath, variant.artifacts, project.properties).analyse(output)
    }

}