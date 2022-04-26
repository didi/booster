package com.didiglobal.booster.task.graph

import com.didiglobal.booster.graph.Node

data class TaskNode(val path: String) : Node {

    override fun toPrettyString(): String = path

}