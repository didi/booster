package com.didiglobal.booster.command

import com.didiglobal.booster.kotlinx.OS
import com.didiglobal.booster.kotlinx.file
import org.apache.commons.io.FileUtils
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import java.io.File

/**
 * Represents a task for command installation
 *
 * @author johnsonlee
 */
@CacheableTask
open class CommandInstaller : DefaultTask() {

    @get:Input
    lateinit var command: Command

    @get:OutputFile
    val location: File
        get() = project.buildDir.file("bin", command.name)

    @TaskAction
    fun install() {
        logger.info("Installing $command => $location")

        this.command.location.openStream().buffered().use { input ->
            FileUtils.copyInputStreamToFile(input, location)
            project.exec {
                it.commandLine = when {
                    OS.isLinux() || OS.isMac() -> listOf("chmod", "+x", location.canonicalPath)
                    OS.isWindows() -> listOf("cmd", "/c echo Y|cacls ${location.canonicalPath} /t /p everyone:f")
                    else -> TODO("Unsupported OS ${OS.name}")
                }
            }
        }
    }

}
