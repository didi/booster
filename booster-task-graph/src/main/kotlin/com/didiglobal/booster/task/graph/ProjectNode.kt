package com.didiglobal.booster.task.graph

import com.didiglobal.booster.graph.Node

data class ProjectNode(val path: String) : Node {

    override fun toPrettyString(): String = path

}