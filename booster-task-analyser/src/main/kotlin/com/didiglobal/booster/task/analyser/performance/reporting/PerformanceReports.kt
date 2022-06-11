package com.didiglobal.booster.task.analyser.performance.reporting

import org.gradle.api.reporting.CustomizableHtmlReport
import org.gradle.api.reporting.ReportContainer
import org.gradle.api.reporting.SingleFileReport
import org.gradle.api.tasks.Internal

interface PerformanceReports : ReportContainer<SingleFileReport> {

    @get:Internal
    val html: CustomizableHtmlReport

    @get:Internal
    val json: SingleFileReport

    @get:Internal
    val dot: SingleFileReport

}