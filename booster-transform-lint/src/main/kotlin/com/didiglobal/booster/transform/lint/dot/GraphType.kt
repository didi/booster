package com.didiglobal.booster.transform.lint.dot

import com.didiglobal.booster.kotlinx.RGB
import com.didiglobal.booster.transform.lint.graph.CallGraph
import com.didiglobal.booster.transform.lint.graph.CallGraphFormatter
import com.didiglobal.booster.transform.lint.palette.WebSafeColorPalette
import com.didiglobal.booster.transform.util.ArgumentsParser

/**
 * Represents the graph type
 *
 * @author johnsonlee
 */
enum class GraphType : CallGraphFormatter {

    DIGRAPH {
        override fun format(graph: CallGraph): CharSequence {
            return StringBuilder().apply {
                appendln("digraph \"${graph.title}\" {")
                appendln("    graph [bgcolor=\"transparent\",pad=\"0.555\"];")
                appendln("    node [color=\"#00BFC4\",fillcolor=\"#00BFC440\",fontcolor=\"#333333\",fontname=Helvetica,shape=box,style=filled];")
                appendln("    edge [fontname=Helvetica];")
                appendln("    rankdir = TB;")
                graph.nodes.joinTo(this, "\n    ", "    ", "\n") { node ->
                    val id = graph.title.substringAfterLast('.')
                    val color = RGB.valueOf(WebSafeColorPalette.random(0x000000, 0xffffff)) // except white color
                    "\"${if (node == CallGraph.ROOT) id else node.toPrettyString()}\" [color=\"#$color\",fillcolor=\"#${color}40\"];"

                }
                graph.joinTo(this, "\n    ", "    ", "\n") { edge ->
                    val id = graph.title.substringAfterLast('.')
                    val color = RGB.valueOf(WebSafeColorPalette.random(0x000000, 0xffffff)).toString() // except white color
                    val from = if (edge.from == CallGraph.ROOT) id else edge.from.toPrettyString()
                    val to = if (edge.to == CallGraph.ROOT) id else edge.to.toPrettyString()
                    "\"$from\" -> \"$to\" [color=\"#$color\",fontcolor=\"#$color\"];"

                }
                appendln("}")
            }
        }
    }

}

internal fun CallGraph.Node.toPrettyString(): String {
    val lp = this.desc.indexOf('(')
    val rp = this.desc.lastIndexOf(')')
    val type = this.type.substringAfterLast('/')
    val desc = ArgumentsParser(this.desc, lp + 1, rp - lp - 1).parse().joinToString(", ", "(", ")") {
        it.substringAfterLast('.')
    }
    return "$type:$name$desc"
}
