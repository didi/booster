package com.didiglobal.booster.command

import com.didiglobal.booster.kotlinx.touch
import java.io.File
import java.net.URL

/**
 * Represents a command line tool
 *
 * @author johnsonlee
 */
open class Command(val name: String, val location: URL) {

    private lateinit var exe: File

    private var installed: Boolean = false

    val executable: File
        get() = exe

    fun install(location: File): Boolean {
        if (installed) return true
        this.location.openStream().buffered().use { input ->
            location.touch().outputStream().buffered().use { output ->
                input.copyTo(output)
            }
        }
        this.exe = location
        this.installed = true
        return true
    }

}