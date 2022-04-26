package com.didiglobal.booster.graph.dot

import com.didiglobal.booster.command.Command
import com.didiglobal.booster.command.CommandService
import com.didiglobal.booster.graph.Graph
import com.didiglobal.booster.graph.GraphRenderer
import com.didiglobal.booster.graph.Node
import com.didiglobal.booster.kotlinx.OS
import com.didiglobal.booster.kotlinx.RGB
import com.didiglobal.booster.kotlinx.execute
import com.didiglobal.booster.kotlinx.stderr
import com.didiglobal.booster.kotlinx.touch
import java.io.File
import java.io.FileNotFoundException
import java.io.IOException

/**
 * Represents the graph type
 *
 * @author johnsonlee
 */
sealed class DotGraph : GraphRenderer {

    object DIGRAPH : DotGraph() {

        override fun <N : Node> render(graph: Graph<N>, prettify: (N) -> String): CharSequence {
            return StringBuilder().apply {
                appendln("digraph \"${graph.title}\" {")
                appendln("    graph [bgcolor=\"transparent\",pad=\"0.555\"];")
                appendln("    node [color=\"#00BFC4\",fillcolor=\"#00BFC440\",fontcolor=\"#333333\",fontname=Helvetica,shape=box,style=filled];")
                appendln("    edge [fontname=Helvetica];")
                appendln("    rankdir = TB;")
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
            format: String = "png",
            dot: Command = CommandService.fromPath("dot${OS.executableSuffix}"),
            prettify: (Node) -> String = Node::toPrettyString
    ) {
        output.touch().writeText(render(graph, prettify).toString())
        dot.location.file.let(::File).takeIf(File::exists)?.let {
            "${it.canonicalPath} -T${format} -O ${output.canonicalPath}".also(::println).execute()
        }?.let { p ->
            p.waitFor()
            if (p.exitValue() != 0) {
                throw IOException(p.stderr)
            }
        } ?: throw FileNotFoundException(dot.location.file)
    }

}
