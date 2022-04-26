package com.didiglobal.booster.task.analyser.reference

import com.didiglobal.booster.graph.GroupedNode

data class ReferenceNode(val component: String, val variant: String, val klass: String) : GroupedNode<String> {

    private val group: String by lazy {
        if (variant == DEFAULT_VARIANT) component else "${component}:${variant}"
    }

    private val symbol: String by lazy {
        klass.replace('/', '.')
    }

    override val groupBy: () -> String = ::group

    override fun toPrettyString() = symbol

}