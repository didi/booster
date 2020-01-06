package com.didiglobal.booster.compression

import com.android.SdkConstants
import com.android.build.gradle.api.BaseVariant
import com.didiglobal.booster.gradle.aapt2Enabled
import com.didiglobal.booster.gradle.preBuildTask
import com.didiglobal.booster.gradle.processResTask
import com.didiglobal.booster.gradle.project
import com.didiglobal.booster.kotlinx.OS
import com.didiglobal.booster.kotlinx.file
import com.didiglobal.booster.compression.task.CompressImages
import org.gradle.api.DefaultTask
import org.gradle.api.Task
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import java.io.File
import kotlin.reflect.KClass

/**
 * Represents a simple implementation of [CompressionTaskCreator]
 *
 * @author johnsonlee
 */
class SimpleCompressionTaskCreator(private val cmdline: CompressionTool, private val selector: (Boolean) -> KClass<out CompressImages<out CompressionOptions>>) : CompressionTaskCreator {

    override fun getCompressionTaskClass(aapt2: Boolean) = selector(aapt2)

    override fun createCompressionTask(variant: BaseVariant, results: CompressionResults, name: String, supplier: () -> Collection<File>, vararg deps: Task): CompressImages<out CompressionOptions> {
        val aapt2 = variant.project.aapt2Enabled
        val install = variant.createCompressionToolIfNotExists()
        return variant.project.tasks.create("compress${variant.name.capitalize()}${name.capitalize()}With${cmdline.name.substringBefore('.').capitalize()}", getCompressionTaskClass(aapt2).java) {
            it.outputs.upToDateWhen { false }
            it.cmdline = cmdline
            it.variant = variant
            it.results = results
            it.supplier = supplier
        }.apply {
            dependsOn(install, deps)
            variant.processResTask.dependsOn(this)
        }
    }

    private fun BaseVariant.createCompressionToolIfNotExists(): Task {
        val name = "install${cmdline.name.substringBefore('.').capitalize()}"
        return project.tasks.findByName(name) ?: project.tasks.create(name, InstallCompressor::class.java) {
            it.outputs.upToDateWhen { false }
            it.cmdline = cmdline
            it.location = project.buildDir.file(SdkConstants.FD_OUTPUT, cmdline.name)
        }.let {
            this.preBuildTask.dependsOn(it)
        }
    }

}

open class InstallCompressor : DefaultTask() {

    lateinit var cmdline: CompressionTool

    @OutputFile
    lateinit var location: File

    @TaskAction
    fun install() {
        logger.info("Installing ${cmdline.name} => ${location.absolutePath}")

        if (cmdline.install(location) && location.exists()) {
            project.exec {
                it.commandLine = when {
                    OS.isLinux() || OS.isMac() -> listOf("chmod", "+x", location.absolutePath)
                    OS.isWindows() -> listOf("cmd", "/c echo Y|cacls ${location.absolutePath} /t /p everyone:f")
                    else -> TODO("Unsupported OS ${OS.name}")
                }
            }
        }
    }

}
