package com.didiglobal.booster.command

import com.didiglobal.booster.kotlinx.touch
import java.io.File
import java.net.URL

/**
 * Represents a command line tool
 *
 * @author johnsonlee
 */
open class Command {

    val name: String

    val location: URL

    private lateinit var exe: File

    private var installed: Boolean = false

    constructor(name: String, location: URL) {
        this.name = name
        this.location = location
    }

    internal constructor(name: String, location: URL, exe: File) : this(name, location) {
        this.exe = exe
        this.installed = exe.exists()
    }

    val executable: File
        get() = exe

    open fun install(location: File): Boolean {
        if (installed) return true
        if (!location.exists()) {
            this.location.openStream().buffered().use { input ->
                location.touch().outputStream().buffered().use { output ->
                    input.copyTo(output)
                }
            }
        }
        this.exe = location
        this.installed = true
        return true
    }

    override fun toString() = "$name => ${if (installed) exe else location}"

}
