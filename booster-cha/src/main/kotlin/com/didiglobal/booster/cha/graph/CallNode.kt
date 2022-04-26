package com.didiglobal.booster.cha.graph

import com.didiglobal.booster.graph.Node
import com.didiglobal.booster.transform.util.ArgumentsParser
import java.util.Objects

open class CallNode internal constructor(val type: String, val name: String, val desc: String, val args: String) : Node {

    constructor(type: String, name: String, desc: String)
            : this(type, name, desc, desc.substring(desc.indexOf('(') + 1, desc.lastIndexOf(')').takeIf { it > -1 }
            ?: desc.length))

    override fun equals(other: Any?) = when {
        other === this -> true
        other is CallNode -> other.type == this.type && other.name == this.name && other.desc == this.desc
        else -> false
    }

    override fun hashCode() = Objects.hash(type, name, desc)

    override fun toString() = "$type.$name$desc"

    override fun toPrettyString(): String {
        val lp = this.desc.indexOf('(')
        val rp = this.desc.lastIndexOf(')')
        val desc = ArgumentsParser(this.desc, lp + 1, rp - lp - 1).parse().joinToString(", ", "(", ")") {
            it.substringAfterLast('.')
        }
        return "$type:$name$desc"
    }

    companion object {

        fun valueOf(s: String): CallNode {
            val lp = s.lastIndexOf('(')
            val dot = s.lastIndexOf('.', lp)
            if (lp < 0 || dot < 0) {
                throw IllegalArgumentException(s)
            }
            return CallNode(s.substring(0, dot), s.substring(dot + 1, lp), s.substring(lp))
        }

    }
}