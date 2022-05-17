package com.didiglobal.booster.graph

/**
 * Represents a graph renderer
 *
 * @author johnsonlee
 */
interface GraphRenderer {

    fun <N : Node> render(graph: Graph<N>, options: Options = EmptyOptions, prettify: (N) -> String = Node::toPrettyString): CharSequence

    interface Options {
        operator fun get(name: String): String?
    }

    object EmptyOptions : Options {
        override fun get(name: String): String? = null
    }

}
