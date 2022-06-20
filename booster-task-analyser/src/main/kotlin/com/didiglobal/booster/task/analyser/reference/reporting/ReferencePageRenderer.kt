package com.didiglobal.booster.task.analyser.reference.reporting

import com.android.build.gradle.api.BaseVariant
import com.didiglobal.booster.cha.asm.Reference
import com.didiglobal.booster.graph.Edge
import com.didiglobal.booster.graph.Graph
import org.gradle.api.Project
import org.gradle.internal.html.SimpleHtmlWriter
import org.gradle.reporting.ReportRenderer
import org.gradle.reporting.TabbedPageRenderer
import java.net.URL

private val URL_STYLE = ReferencePageRenderer::class.java.getResource("/style.css")!!

class ReferencePageRenderer(
        private val project: Project,
        private val variant: BaseVariant?
) : TabbedPageRenderer<Graph<Reference>>() {

    private val _title: String by lazy {
        if (variant == null) project.name else "${project.name}:${variant.name}"
    }

    override fun getTitle(): String = _title

    override fun getHeaderRenderer(): ReportRenderer<Graph<Reference>, SimpleHtmlWriter> = object : ReportRenderer<Graph<Reference>, SimpleHtmlWriter>() {
        override fun render(model: Graph<Reference>, output: SimpleHtmlWriter) {
            output.startElement("p").attribute("class", "subtitle").characters(project.description ?: "").endElement()
        }
    }

    override fun getContentRenderer(): ReportRenderer<Graph<Reference>, SimpleHtmlWriter> = object : ReportRenderer<Graph<Reference>, SimpleHtmlWriter>() {
        override fun render(model: Graph<Reference>, output: SimpleHtmlWriter) {
            output.run {
                model.groupBy {
                    it.to.groupBy()
                }.toSortedMap(Reference.COMPONENT_COMPARATOR).map { (group, edges) ->
                    group to edges.mapTo(sortedSetOf(compareBy(Reference::klass)), Edge<Reference>::to)
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
