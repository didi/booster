package com.didiglobal.booster.command

import java.io.Serializable
import java.net.URL

/**
 * Represents a command line tool
 *
 * @author johnsonlee
 */
open class Command(val name: String, val location: URL) : Serializable {

    override fun equals(other: Any?) = when {
        this === other -> true
        other is Command -> name == other.name && location == other.location
        else -> false
    }

    override fun hashCode(): Int {
        return arrayOf(name, location).contentHashCode()
    }

    override fun toString() = "$name:$location"

}
