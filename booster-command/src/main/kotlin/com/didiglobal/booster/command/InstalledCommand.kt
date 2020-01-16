package com.didiglobal.booster.command

import java.io.File

/**
 * Represents a command which already installed
 *
 * @author johnsonlee
 */
internal class InstalledCommand(name: String, exe: File) : Command(name, exe.toURI().toURL(), exe) {

    override fun install(location: File) = true

}