package com.didiglobal.booster.cha.graph

import com.didiglobal.booster.graph.Node

data class ClassNode(
        val name: String
) : Node {

    override fun toPrettyString(): String = name

}