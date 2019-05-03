package com.didiglobal.booster.transform.lint.graph

import java.io.PrintWriter
import java.util.Objects

/**
 * Represents the call graph
 *
 * @author johnsonlee
 */
class CallGraph private constructor(private val edges: Map<Node, Set<Node>>, val title: String = "") : Iterable<CallGraph.Edge> {

    companion object {
        /**
         * A virtual root node of call graph
         */
        val ROOT = Node("*", "*", "*")
    }

    val nodes: Collection<Node>
        get() = this.map {
            listOf(it.from, it.to)
        }.flatten().toSet()

    operator fun get(node: Node): Set<Node> = edges[node] ?: emptySet()

    /**
     * Print this call graph
     */
    fun print(out: PrintWriter = PrintWriter(System.out, true), transform: (CallGraph) -> CharSequence) {
        out.println(transform(this))
    }

    override fun iterator(): Iterator<Edge> {
        return edges.map { pair ->
            pair.value.map {
                Edge(pair.key, it)
            }.toSet()
        }.flatten().iterator()
    }

    class Node(val type: String, val name: String, val desc: String) {

        override fun equals(other: Any?) = when {
            other === this -> true
            other is Node -> other.type == this.type && other.name == this.name && other.desc == this.desc
            else -> false
        }

        override fun hashCode() = Objects.hash(type, name, desc)

        override fun toString() = "$type.$name$desc"

        companion object {

            fun valueOf(s: String): Node {
                val lp = s.lastIndexOf('(')
                val dot = s.lastIndexOf('.', lp)
                if (lp < 0 || dot < 0) {
                    throw IllegalArgumentException(s)
                }
                return Node(s.substring(0, dot), s.substring(dot + 1, lp), s.substring(lp))
            }

        }
    }

    class Edge(val from: Node, val to: Node) {

        override fun hashCode() = Objects.hash(from, to)

        override fun equals(other: Any?) = when {
            other === this -> true
            other is Edge -> other.from == this.from && other.to == this.to
            else -> false
        }

        override fun toString() = "$from -> $to"

    }

    class Builder {

        private val edges = mutableMapOf<Node, MutableSet<Node>>()

        private var title = ""

        fun getTitle() = title

        fun setTitle(title: String) = this.also {
            this.title = title
        }

        fun addEdges(vararg edges: Edge) = this.also {
            edges.forEach {
                addEdge(it.from, it.to)
            }
        }

        fun hasEdge(edge: Edge) = this.hasEdge(edge.from, edge.to)

        fun hasEdge(from: Node, to: Node) = this.edges.containsKey(from) && this.edges[from]?.contains(to) == true

        fun addEdge(from: Node, to: Node) = this.also {
            edges.getOrPut(from) {
                mutableSetOf()
            }.add(to)
        }

        fun build() = CallGraph(this.edges, this.title)

    }

}

fun Iterable<CallGraph.Node>.toEdges(): Collection<CallGraph.Edge> {
    val iterator = iterator()
    if (!iterator.hasNext()) {
        return emptyList()
    }

    val result = mutableListOf<CallGraph.Edge>()
    var current = iterator.next()
    while (iterator.hasNext()) {
        val next = iterator.next()
        result.add(CallGraph.Edge(current, next))
        current = next
    }

    return result
}
