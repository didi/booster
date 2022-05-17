package com.didiglobal.booster.graph

import java.io.Serializable
import kotlin.test.Test
import kotlin.test.assertEquals

data class ValueNode(val value: Serializable) : Node

class GraphTest {

    @Test
    fun `test edges`() {
        val graph = Graph.Builder<ValueNode>()
                .addEdge(ValueNode(1), ValueNode('a'))
                .addEdge(ValueNode(2), ValueNode('a'))
                .addEdge(ValueNode(2), ValueNode('b'))
                .addEdge(ValueNode(3), ValueNode('a'))
                .addEdge(ValueNode(3), ValueNode('b'))
                .addEdge(ValueNode(3), ValueNode('c'))
                .build()
        assertEquals(
                listOf(
                        Edge(ValueNode(1), ValueNode('a')),
                        Edge(ValueNode(2), ValueNode('a')),
                        Edge(ValueNode(2), ValueNode('b')),
                        Edge(ValueNode(3), ValueNode('a')),
                        Edge(ValueNode(3), ValueNode('b')),
                        Edge(ValueNode(3), ValueNode('c'))
                ),
                graph.toList()
        )
    }

    @Test
    fun `test nodes`() {
        val graph = Graph.Builder<ValueNode>()
                .addEdge(ValueNode(1), ValueNode('a'))
                .addEdge(ValueNode(2), ValueNode('a'))
                .addEdge(ValueNode(2), ValueNode('b'))
                .addEdge(ValueNode(3), ValueNode('a'))
                .addEdge(ValueNode(3), ValueNode('b'))
                .addEdge(ValueNode(3), ValueNode('c'))
                .build()
        assertEquals(
                setOf(
                        ValueNode(1),
                        ValueNode(2),
                        ValueNode(3),
                        ValueNode('a'),
                        ValueNode('b'),
                        ValueNode('c')
                ),
                graph.nodes
        )
    }

}