package com.didiglobal.booster.cha.graph

import com.didiglobal.booster.transform.util.ArgumentsParser
import java.io.PrintWriter
import java.util.Collections
import java.util.Objects
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArraySet

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
        val ROOT = object : Node("Main", "main", "()V") {
            override fun toPrettyString() = toString()
            override fun hashCode() = super.hashCode() * 31 + javaClass.name.hashCode()
            override fun equals(other: Any?) = this === other
        }
    }

    val nodes: Collection<Node>
        get() = this.map {
            listOf(it.from, it.to)
        }.flatten().toSet()

    operator fun get(node: Node): Set<Node> = Collections.unmodifiableSet(edges[node] ?: emptySet())

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

    open class Node internal constructor(val type: String, val name: String, val desc: String, val args: String) {

        constructor(type: String, name: String, desc: String)
                : this(type, name, desc, desc.substring(desc.indexOf('(') + 1, desc.lastIndexOf(')')))

        override fun equals(other: Any?) = when {
            other === this -> true
            other is Node -> other.type == this.type && other.name == this.name && other.desc == this.desc
            else -> false
        }

        override fun hashCode() = Objects.hash(type, name, desc)

        override fun toString() = "$type.$name$desc"

        open fun toPrettyString(): String {
            val lp = this.desc.indexOf('(')
            val rp = this.desc.lastIndexOf(')')
            val desc = ArgumentsParser(this.desc, lp + 1, rp - lp - 1).parse().joinToString(", ", "(", ")") {
                it.substringAfterLast('.')
            }
            return "$type:$name$desc"
        }

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

        private val edges = ConcurrentHashMap<Node, MutableSet<Node>>()

        private var title = ""

        fun getTitle() = title

        fun setTitle(title: String) = apply {
            this.title = title
        }

        fun addEdge(edge: Edge) = addEdge(edge.from, edge.to)

        fun addEdge(from: Node, to: Node) = apply {
            edges.getOrPut(from, ::CopyOnWriteArraySet) += to
        }

        fun addEdges(from: Node, to: Iterable<Node>) = apply {
            edges.getOrPut(from, ::CopyOnWriteArraySet) += to
        }

        fun addEdges(chain: Iterable<Node>) = apply {
            chain.zipWithNext(CallGraph::Edge).forEach {
                this.addEdge(it)
            }
        }

        fun hasEdge(edge: Edge) = this.hasEdge(edge.from, edge.to)

        fun hasEdge(from: Node, to: Node) = this.edges.containsKey(from) && this.edges[from]?.contains(to) == true

        fun build() = CallGraph(this.edges, this.title)

    }

}
