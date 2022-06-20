package com.didiglobal.booster.cha.asm

import com.didiglobal.booster.graph.GroupedNode

data class Reference(
        val component: String,
        val klass: String
) : GroupedNode<String> {

    companion object {
        val COMPONENT_COMPARATOR = Comparator<String> { a, b ->
            val ap = a.startsWith("project ")
            val bp = b.startsWith("project ")
            when {
                (ap && bp) || (!ap && !bp) -> a.compareTo(b)
                ap -> -1
                else -> 1
            }
        }
    }

    private val symbol: String by lazy {
        klass.replace('/', '.')
    }

    override val groupBy: () -> String = ::component

    override fun toPrettyString() = symbol

}