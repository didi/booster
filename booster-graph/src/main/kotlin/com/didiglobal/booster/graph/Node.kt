package com.didiglobal.booster.graph

interface Node {
    fun toPrettyString() = toString()
    override fun hashCode(): Int
    override fun equals(other: Any?): Boolean
}