package com.didiglobal.booster.cha.graph

/**
 * A virtual root node of call graph
 */
val ROOT = object : CallNode("Main", "main", "()V") {
    override fun toPrettyString() = toString()
    override fun hashCode() = super.hashCode() * 31 + javaClass.name.hashCode()
    override fun equals(other: Any?) = this === other
}
