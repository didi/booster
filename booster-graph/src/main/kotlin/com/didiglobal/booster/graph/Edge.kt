package com.didiglobal.booster.graph

data class Edge<T : Node>(val from: T, val to: T) {

    override fun toString() = "$from -> $to"

}