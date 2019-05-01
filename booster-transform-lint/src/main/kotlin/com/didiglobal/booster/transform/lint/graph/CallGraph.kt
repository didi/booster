package com.didiglobal.booster.transform.lint.graph

import java.util.Objects

/**
 * Represents the call graph
 *
 * @author johnsonlee
 */
class CallGraph private constructor(val edges: Map<Node, Set<Node>>) : Iterable<CallGraph.Edge> {

    companion object {
        val ROOT = Node()
    }

    override fun iterator(): Iterator<Edge> {
        return edges.map { pair ->
            pair.value.map {
                Edge(pair.key, it)
            }.toSet()
        }.flatten().iterator()
    }

    class Node(val type: String, val name: String, val desc: String) {

        internal constructor(): this("", "", "")

        override fun equals(other: Any?): Boolean {
            if (this === other) {
                return true
            }

            if (other !is Node) {
                return false
            }

            return type == other.type && name == other.name && desc == other.desc
        }

        override fun hashCode(): Int {
            return Objects.hash(type, name, desc)
        }

        override fun toString(): String {
            return "$type.$name$desc"
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

        override fun hashCode(): Int {
            return Objects.hash(from, to)
        }

        override fun equals(other: Any?): Boolean {
            if (this === other) {
                return true
            }

            if (other !is Edge) {
                return false
            }

            return from == other.from && to == other.to
        }

        override fun toString(): String {
            return "$from -> $to"
        }
    }

    class Builder {

        private val edges = mutableMapOf<Node, MutableSet<Node>>()

        fun addEdge(edge: Edge): Builder {
            this.edges.getOrPut(edge.from) {
                mutableSetOf()
            }.add(edge.to)
            return this
        }

        fun hasEdge(edge: Edge): Boolean {
            return this.hasEdge(edge.from, edge.to)
        }

        fun hasEdge(from: Node, to: Node): Boolean {
            return this.edges.containsKey(from) && this.edges[from]?.contains(to) == true
        }

        fun addEdge(from: Node, to: Node): Builder {
            this.edges.getOrPut(from) {
                mutableSetOf()
            }.add(to)
            return this
        }

        fun build(): CallGraph {
            return CallGraph(this.edges)
        }

    }

}
