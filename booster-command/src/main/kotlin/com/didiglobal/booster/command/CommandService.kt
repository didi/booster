package com.didiglobal.booster.command

import com.didiglobal.booster.build.BoosterServiceLoader
import java.io.File

/**
 * Represents a command service
 */
class CommandService {

    companion object {

        private val commands = BoosterServiceLoader.load(CommandProvider::class.java, Command::class.java.classLoader).map {
            it.get()
        }.flatten().map {
            it.name to it
        }.toMap()

        fun get(name: String): Command = commands[name] ?: fromPath(name)

        fun fromPath(name: String): Command = System.getenv("PATH").split(File.pathSeparatorChar).map {
            File(it)
        }.map { path ->
            when {
                path.isDirectory -> path.listFiles { file ->
                    file.name == name && !file.isDirectory
                }?.firstOrNull()
                else -> if (path.name == name) path else null
            }
        }.find {
            it != null && it.exists()
        }?.canonicalFile?.let {
            InstalledCommand(name, it)
        } ?: NoneCommand(name)
    }

}