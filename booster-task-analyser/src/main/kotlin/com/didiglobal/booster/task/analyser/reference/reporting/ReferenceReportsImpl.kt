package com.didiglobal.booster.task.analyser.reference.reporting

import org.gradle.api.Task
import org.gradle.api.internal.CollectionCallbackActionDecorator
import org.gradle.api.reporting.CustomizableHtmlReport
import org.gradle.api.reporting.SingleFileReport
import org.gradle.api.reporting.internal.CustomizableHtmlReportImpl
import org.gradle.api.reporting.internal.TaskGeneratedSingleFileReport
import org.gradle.api.reporting.internal.TaskReportContainer
import javax.inject.Inject

open class ReferenceReportsImpl @Inject constructor(
        task: Task,
        callbackActionDecorator: CollectionCallbackActionDecorator
) : TaskReportContainer<SingleFileReport>(SingleFileReport::class.java, task, callbackActionDecorator), ReferenceReports {

    init {
        add(CustomizableHtmlReportImpl::class.java, "html", task)
        add(TaskGeneratedSingleFileReport::class.java, "json", task)
        add(TaskGeneratedSingleFileReport::class.java, "dot", task)
    }

    override val html: CustomizableHtmlReport
        get() = withType(CustomizableHtmlReport::class.java).getByName("html")

    override val json: SingleFileReport
        get() = getByName("json")

    override val dot: SingleFileReport
        get() = getByName("dot")

}