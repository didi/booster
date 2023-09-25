package com.didiglobal.booster.task.analyser.reference

import com.android.build.api.variant.Variant
import com.didiglobal.booster.cha.asm.Reference
import com.didiglobal.booster.graph.Graph
import org.gradle.api.Project
import org.gradle.internal.html.SimpleHtmlWriter
import org.gradle.reporting.ReportRenderer
import org.gradle.reporting.TabbedPageRenderer
import java.net.URL

class ReferencePageRenderer(
        private val project: Project,
        private val variant: Variant?
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
                    group to edges.groupBy({ it.to.klass }) { it.from.klass }
                }.forEach { (title, references) ->
                    startElement("h2").characters(title).endElement()
                    startElement("ul").attribute("class", "refs")
                    references.forEach { (ref, sources) ->
                        startElement("li")
                        startElement("div").characters(ref).endElement()
                        startElement("ul")
                        sources.forEach {
                            startElement("li").characters(it).endElement()
                        }
                        endElement()
                        endElement()
                    }
                    endElement()
                }
            }
        }

    }

    override fun getStyleUrl(): URL = ReferencePageRenderer::class.java.getResource("/style.css")

}
