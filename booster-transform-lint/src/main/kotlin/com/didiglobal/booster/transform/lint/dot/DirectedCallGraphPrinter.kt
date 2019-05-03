package com.didiglobal.booster.transform.lint.dot

import com.didiglobal.booster.kotlinx.RGB
import com.didiglobal.booster.transform.lint.graph.CallGraph
import com.didiglobal.booster.transform.lint.graph.CallGraph.Companion.ROOT
import com.didiglobal.booster.transform.lint.graph.CallGraphPrinter
import com.didiglobal.booster.transform.lint.palette.WEB_SAFE_COLOR_PALETTE
import com.didiglobal.booster.transform.util.ArgumentsParser
import java.io.BufferedWriter
import java.io.File
import java.io.OutputStream
import java.io.OutputStreamWriter
import java.io.Writer
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets

/**
 * Represents a directed call graph printer
 *
 * @author johnsonlee
 */
class DirectedCallGraphPrinter(private val writer: Writer, private val autoFlush: Boolean = true) : CallGraphPrinter(writer, autoFlush) {

    constructor(output: OutputStream, autoFlush: Boolean = true) : this(BufferedWriter(OutputStreamWriter(output)), autoFlush)

    constructor(output: File, charset: Charset = StandardCharsets.UTF_8, autoFlush: Boolean = true) : this(BufferedWriter(OutputStreamWriter(output.outputStream(), charset)), autoFlush)

    constructor(path: String, charset: Charset = StandardCharsets.UTF_8, autoFlush: Boolean = true) : this(File(path), charset, autoFlush)

    override fun print(graph: CallGraph) {
        println("""
        digraph "${graph.title}" {
            graph [bgcolor="transparent",pad="0.555"];
            node [color="#00BFC4",fillcolor="#00BFC440",fontcolor="#333333",fontname=Helvetica,shape=box,style=filled];
            edge [fontname=Helvetica];
            rankdir = LR;
        """.trimIndent())

        val title = graph.title.substring(graph.title.lastIndexOf('.') + 1)

        graph.nodes.forEach {
            val color = RGB.valueOf(WEB_SAFE_COLOR_PALETTE.random(0x000000, 0xffffff)) // except white color
            println("    \"${if (it == ROOT) title else it.toPrettyString()}\" [color=\"#$color\",fillcolor=\"#${color}40\"];")
        }

        //  edge list
        graph.forEach { edge ->
            val color = RGB.valueOf(WEB_SAFE_COLOR_PALETTE.random(0x000000, 0xffffff)).toString() // except white color
            val from = if (edge.from == ROOT) title else edge.from.toPrettyString()
            val to = if (edge.to == ROOT) title else edge.to.toPrettyString()
            println("    \"$from\" -> \"$to\" [color=\"#$color\",fontcolor=\"#$color\"];")
        }

        println("}")
    }

}

internal fun CallGraph.Node.toPrettyString(): String {
    val lp = this.desc.indexOf('(')
    val rp = this.desc.lastIndexOf(')')
    val type = this.type.substring(this.type.lastIndexOf('/') + 1)
    val desc = ArgumentsParser(this.desc, lp + 1, rp - lp - 1).parse().map {
        val dot = it.lastIndexOf('.')
        if (dot < 0) it else it.substring(dot + 1)
    }.joinToString(", ", "(", ")")
    return "$type:$name$desc"
}

