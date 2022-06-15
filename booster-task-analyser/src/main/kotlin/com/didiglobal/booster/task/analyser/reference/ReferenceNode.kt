package com.didiglobal.booster.task.analyser.reference

import com.android.build.gradle.api.BaseVariant
import com.didiglobal.booster.graph.GroupedNode

data class ReferenceNode(
        val component: String,
        val klass: String,
        val variant: BaseVariant? = null
) : GroupedNode<String> {

    private val group: String by lazy {
        if (variant == null) component else "${component}:${variant.name}"
    }

    private val symbol: String by lazy {
        klass.replace('/', '.')
    }

    override val groupBy: () -> String = ::group

    override fun toPrettyString() = symbol

}