package com.didiglobal.booster.task.analyser.reference.reporting

import com.android.build.gradle.api.BaseVariant
import com.didiglobal.booster.graph.Edge
import com.didiglobal.booster.graph.Graph
import com.didiglobal.booster.task.analyser.reference.ReferenceNode
import org.gradle.api.Project
import org.gradle.internal.html.SimpleHtmlWriter
import org.gradle.reporting.ReportRenderer
import org.gradle.reporting.TabbedPageRenderer
import java.net.URL
import java.util.TreeMap

private val URL_STYLE = ReferencePageRenderer::class.java.getResource("/style.css")!!

class ReferencePageRenderer(
        private val project: Project,
        private val variant: BaseVariant?
) : TabbedPageRenderer<Graph<ReferenceNode>>() {

    private val _title: String by lazy {
        if (variant == null) project.name else "${project.name}:${variant.name}"
    }

    override fun getTitle(): String = _title

    override fun getHeaderRenderer(): ReportRenderer<Graph<ReferenceNode>, SimpleHtmlWriter> = object : ReportRenderer<Graph<ReferenceNode>, SimpleHtmlWriter>() {
        override fun render(model: Graph<ReferenceNode>, output: SimpleHtmlWriter) {
            output.startElement("p").attribute("class", "subtitle").characters(project.description ?: "").endElement()
        }
    }

    override fun getContentRenderer(): ReportRenderer<Graph<ReferenceNode>, SimpleHtmlWriter> = object : ReportRenderer<Graph<ReferenceNode>, SimpleHtmlWriter>() {
        override fun render(model: Graph<ReferenceNode>, output: SimpleHtmlWriter) {
            output.run {
                model.groupBy {
                    it.to.groupBy()
                }.toSortedMap(ReferenceNode.COMPONENT_COMPARATOR).map { (group, edges) ->
                    group to edges.mapTo(sortedSetOf(compareBy(ReferenceNode::klass)), Edge<ReferenceNode>::to)
                }.forEach { (title, items) ->
                    startElement("h2").characters(title).endElement()
                    startElement("ul")
                    items.forEach { item ->
                        startElement("li").characters(item.klass).endElement()
                    }
                    endElement()
                }
            }
        }

    }

    override fun getStyleUrl(): URL = URL_STYLE

}
