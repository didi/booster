package com.didiglobal.booster.graph.json

import com.didiglobal.booster.graph.Graph
import com.didiglobal.booster.graph.GraphRenderer
import com.didiglobal.booster.graph.Node

object JsonGraphRender : GraphRenderer {

    override fun <N : Node> render(graph: Graph<N>, options: GraphRenderer.Options, prettify: (N) -> String): CharSequence {
        return graph.joinToString(",\n", "[\n", "\n]") {
            """
            |  {
            |    "from": ${prettify(it.from)},
            |    "to": ${prettify(it.to)}
            |  }
            """.trimMargin()
        }
    }

}
