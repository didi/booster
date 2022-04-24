package com.didiglobal.booster.task.graph

import com.didiglobal.booster.cha.graph.CallGraph

data class TaskNode(val path: String) : CallGraph.Node("", path, "") {

    override fun toPrettyString(): String = path

}