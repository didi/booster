package com.didiglobal.booster.task.analyser

import com.didiglobal.booster.kotlinx.file
import org.gradle.api.internal.AbstractTask
import org.gradle.api.reporting.ReportContainer
import org.gradle.api.reporting.Reporting
import org.gradle.api.reporting.ReportingExtension
import org.gradle.api.reporting.SingleFileReport

fun <T> T.configureReportConvention(analysis: String, variant: String)
    where T : AbstractTask, T: Reporting<out ReportContainer<out SingleFileReport>> {
    reports.all {
        it.outputLocation.convention(project.layout.projectDirectory.file(project.provider<String> {
            val base = project.extensions.getByType(ReportingExtension::class.java).baseDir
            base.file(Build.ARTIFACT, analysis, variant, it.name, "index.${it.name}").absolutePath
        }))
    }
}