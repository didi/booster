package com.didiglobal.booster.graph.dot

import com.didiglobal.booster.command.Command
import com.didiglobal.booster.command.CommandService
import com.didiglobal.booster.graph.Graph
import com.didiglobal.booster.graph.GraphRenderer
import com.didiglobal.booster.graph.GroupedNode
import com.didiglobal.booster.graph.Node
import com.didiglobal.booster.kotlinx.*
import java.io.File
import java.io.FileNotFoundException
import java.io.IOException
import kotlin.reflect.full.memberProperties

private val DOT = CommandService.fromPath("dot${OS.executableSuffix}")

/**
 * Represents the graph type
 *
 * @author johnsonlee
 */
sealed class DotGraph : GraphRenderer {

    object DIGRAPH : DotGraph() {

        override fun <N : Node> render(graph: Graph<N>, options: GraphRenderer.Options, prettify: (N) -> String): CharSequence {
            return StringBuilder().apply {
                appendln("digraph \"${graph.title}\" {")
                appendln("    graph [bgcolor=\"transparent\",pad=\"0.555\"];")
                appendln("    node [color=\"#00BFC4\",fillcolor=\"#00BFC440\",fontcolor=\"#333333\",fontname=Helvetica,shape=box,style=filled];")
                appendln("    edge [fontname=Helvetica];")
                appendln("    rankdir = ${options["rankdir"] ?: "TB"};")

                graph.nodes.filterIsInstance<GroupedNode<*>>().groupBy {
                    it.groupBy() ?: ""
                }.entries.withIndex().forEach { (index, entry) ->
                    val color = RGB.valueOf(WebSafeColorPalette.random(0x000000, 0xffffff))
                    appendln("    subgraph cluster_${index} {")
                    appendln("        style=\"rounded,dashed\";")
                    appendln("        label=\"${entry.key}\";")
                    appendln("        fgcolor=\"${color}\";")
                    entry.value.map { node ->
                        @Suppress("UNCHECKED_CAST") (node as N)
                    }.forEach { node ->
                        appendln("        \"${prettify(node)}\";")
                    }
                    appendln("    }")
                }

                graph.nodes.joinTo(this, "\n    ", "    ", "\n") { node ->
                    val color = RGB.valueOf(WebSafeColorPalette.random(0x000000, 0xffffff)) // except white color
                    "\"${prettify(node)}\" [color=\"#$color\",fillcolor=\"#${color}40\"];"

                }
                graph.joinTo(this, "\n    ", "    ", "\n") { edge ->
                    val color = RGB.valueOf(WebSafeColorPalette.random(0x000000, 0xffffff)).toString() // except white color
                    "\"${prettify(edge.from)}\" -> \"${prettify(edge.to)}\" [color=\"#$color\",fontcolor=\"#$color\"];"
                }
                appendln("}")
            }
        }

    }

    fun <N : Node> visualize(
            graph: Graph<N>,
            output: File,
            options: DotOptions,
            dot: Command = DOT,
            prettify: (Node) -> String = Node::toPrettyString
    ) {
        output.touch().writeText(render(graph, options, prettify).toString())
        dot.execute("-T${options.format}", "-O", output.canonicalPath)
    }

    fun <N : Node> visualize(
            graph: Graph<N>,
            output: File,
            format: String = "png",
            dot: Command = DOT,
            prettify: (Node) -> String = Node::toPrettyString
    ) = visualize(graph, output, DotOptions(format), dot, prettify)

    data class DotOptions(
            val format: String = "png",
            val rankdir: String = "TB"
    ) : GraphRenderer.Options {

        private val options = DotOptions::class.memberProperties.associateBy { it.name }.withDefault { null }

        override fun get(name: String): String? = options[name]?.get(this)?.toString()

    }

}
