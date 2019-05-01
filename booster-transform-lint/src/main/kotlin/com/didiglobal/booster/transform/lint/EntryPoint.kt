package com.didiglobal.booster.transform.lint

import java.util.Objects

/**
 * Represents the entry point of main thread / UI thread
 *
 * @author johnsonlee
 */
internal class EntryPoint(val name: String, val desc: String) {

    override fun hashCode(): Int {
        return Objects.hash(name, desc)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) {
            return true
        }
        if (other !is EntryPoint) {
            return false
        }

        return name == other.name && desc == other.desc
    }

    override fun toString(): String {
        return "$name$desc"
    }

    companion object {

        fun valueOf(signature: String): EntryPoint {
            val p = signature.indexOf('(')
            if (p < 0) {
                throw IllegalArgumentException(signature)
            }
            return EntryPoint(signature.substring(0, p), signature.substring(p))
        }

    }

}
