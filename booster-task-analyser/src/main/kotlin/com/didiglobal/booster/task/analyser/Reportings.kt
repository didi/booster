package com.didiglobal.booster.task.analyser

import com.android.build.gradle.api.BaseVariant
import org.gradle.api.internal.AbstractTask
import org.gradle.api.reporting.ReportContainer
import org.gradle.api.reporting.Reporting
import org.gradle.api.reporting.ReportingExtension
import org.gradle.api.reporting.SingleFileReport
import java.io.File

fun <T> T.configureReportConvention(analysis: String, variant: BaseVariant?)
    where T : AbstractTask, T: Reporting<out ReportContainer<out SingleFileReport>> {
    reports.all {
        it.outputLocation.convention(project.layout.projectDirectory.file(project.provider<String> {
            val base = project.extensions.getByType(ReportingExtension::class.java).baseDir
            val path = listOfNotNull(Build.ARTIFACT, analysis, variant?.name, it.name, "index.${it.name}").joinToString(File.separator)
            File(base, path).absolutePath
        }))
    }
}