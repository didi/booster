package com.didiglobal.booster.command

import com.didiglobal.booster.kotlinx.stderr
import java.io.File
import java.io.IOException
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

    @Throws(IOException::class)
    open fun execute(vararg args: String) {
        Runtime.getRuntime().exec(arrayOf(location.file.let(::File).canonicalPath) + args).let { p ->
            p.waitFor()
            if (p.exitValue() != 0) {
                throw IOException(p.stderr)
            }
        }
    }

    override fun hashCode(): Int {
        return arrayOf(name, location).contentHashCode()
    }

    override fun toString() = "$name:$location"

}
