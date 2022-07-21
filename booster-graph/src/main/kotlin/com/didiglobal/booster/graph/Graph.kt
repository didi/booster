package com.didiglobal.booster.graph

import java.io.PrintWriter
import java.util.Collections
import java.util.Stack
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
        Collections.unmodifiableList(edges.entries.fold(mutableListOf<Edge<N>>()) { acc, entry ->
            entry.value.forEach { value ->
                acc.add(Edge(entry.key, value))
            }
            acc
        })
    }

    val nodes: Collection<N> by lazy {
        Collections.unmodifiableSet(fold(mutableSetOf<N>()) { acc, edge ->
            acc.add(edge.from)
            acc.add(edge.to)
            acc
        })
    }

    operator fun get(from: N): Set<N> = edges[from]?.let(Collections::unmodifiableSet) ?: emptySet()

    fun getPredecessors(node: N): Set<N> {
        val stack = Stack<N>().apply {
            push(node)
        }
        val predecessors = mutableSetOf<N>()
        while (stack.isNotEmpty()) {
            val current = stack.pop()
            predecessors += entries.filter {
                it.to == current && it.from !in predecessors
            }.map {
                it.from
            }.onEach {
                stack.push(it)
            }
        }
        return predecessors
    }

    fun getSuccessors(node: N): Set<N> {
        val stack = Stack<N>().apply {
            push(node)
        }
        val successors = mutableSetOf<N>()
        while (stack.isNotEmpty()) {
            val current = stack.pop()
            successors += get(current).filter {
                it !in successors
            }.onEach {
                stack.push(it)
            }
        }
        return successors
    }

    fun reversed(): Graph<N> {
        return edges.entries.fold(Builder<N>()) { builder, entry ->
            entry.value.forEach { to ->
                builder.addEdge(to, entry.key)
            }
            builder
        }.build()
    }

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