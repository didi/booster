package com.didiglobal.booster.graph

interface GroupedNode<T> : Node {

    val groupBy: () -> T

}