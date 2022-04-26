package com.didiglobal.booster.graph

import java.io.PrintWriter
import java.util.Collections
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArraySet

/**
 * Represents a graph
 *
 * @author johnsonlee
 */
class Graph<N : Node> private constructor(
        private val edges: Map<N, Set<N>>,
        val title: String = ""
) : Iterable<Edge<N>> {

    private val entries: List<Edge<N>> by lazy {
        edges.map { pair ->
            pair.value.map {
                Edge(pair.key, it)
            }.toSet()
        }.flatten()
    }

    val nodes: Collection<N> by lazy {
        this.map {
            listOf(it.from, it.to)
        }.flatten().toSet()
    }

    operator fun get(node: N): Set<N> = Collections.unmodifiableSet(edges[node] ?: emptySet())

    /**
     * Print this graph
     */
    fun print(out: PrintWriter = PrintWriter(System.out, true), transform: (Graph<N>) -> CharSequence) {
        out.println(transform(this))
    }

    override fun iterator(): Iterator<Edge<N>> = entries.iterator()

    class Builder<N : Node> {

        private val edges = ConcurrentHashMap<N, MutableSet<N>>()

        private var title = ""

        fun getTitle() = title

        fun setTitle(title: String) = apply {
            this.title = title
        }

        fun addEdge(edge: Edge<N>) = addEdge(edge.from, edge.to)

        fun addEdge(from: N, to: N) = apply {
            edges.getOrPut(from, ::CopyOnWriteArraySet) += to
        }

        fun addEdges(from: N, to: Iterable<N>) = apply {
            edges.getOrPut(from, ::CopyOnWriteArraySet) += to
        }

        fun addEdges(chain: Iterable<N>) = apply {
            chain.zipWithNext(::Edge).forEach {
                this.addEdge(it)
            }
        }

        fun hasEdge(edge: Edge<N>) = this.hasEdge(edge.from, edge.to)

        fun hasEdge(from: N, to: N) = this.edges.containsKey(from) && this.edges[from]?.contains(to) == true

        fun build(): Graph<N> = Graph(Collections.unmodifiableMap(this.edges), this.title)
    }

}