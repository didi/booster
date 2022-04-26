package com.didiglobal.booster.graph

/**
 * Represents a graph renderer
 *
 * @author johnsonlee
 */
interface GraphRenderer {

    fun <N : Node> render(graph: Graph<N>, prettify: (N) -> String = Node::toPrettyString): CharSequence

}
