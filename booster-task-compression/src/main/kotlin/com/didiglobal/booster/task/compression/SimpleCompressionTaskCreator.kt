package com.didiglobal.booster.task.compression

import com.android.SdkConstants
import com.android.build.gradle.api.BaseVariant
import com.didiglobal.booster.gradle.aapt2Enabled
import com.didiglobal.booster.gradle.mergeAssetsTask
import com.didiglobal.booster.gradle.mergeResourcesTask
import com.didiglobal.booster.gradle.mergedAssets
import com.didiglobal.booster.gradle.mergedRes
import com.didiglobal.booster.gradle.preBuildTask
import com.didiglobal.booster.gradle.processResTask
import com.didiglobal.booster.gradle.project
import com.didiglobal.booster.gradle.scope
import com.didiglobal.booster.kotlinx.OS
import com.didiglobal.booster.kotlinx.file
import com.didiglobal.booster.util.search
import org.gradle.api.DefaultTask
import org.gradle.api.Task
import org.gradle.api.tasks.TaskAction
import java.io.File
import kotlin.reflect.KClass

/**
 * Represents a simple implementation of [CompressionTaskCreator]
 *
 * @author johnsonlee
 */
class SimpleCompressionTaskCreator(private val cmdline: CompressionTool, private val selector: (Boolean) -> KClass<out CompressImages>) : CompressionTaskCreator {

    override fun getCompressionTaskClass(aapt2: Boolean) = selector(aapt2)

    override fun createAssetsCompressionTask(variant: BaseVariant, results: CompressionResults): Task {
        val install = variant.createCompressionToolIfNotExists()
        return variant.project.tasks.create("compress${variant.name.capitalize()}AssetsWith${cmdline.name.substringBefore('.').capitalize()}", getCompressionTaskClass(false).java) {
            it.outputs.upToDateWhen { false }
            it.cmdline = cmdline
            it.variant = variant
            it.results = results
            it.sources = { variant.scope.mergedAssets.search(::isPng) }
        }.apply {
            dependsOn(install, variant.mergeAssetsTask)
            variant.processResTask.dependsOn(this)
        }
    }

    override fun createResourcesCompressionTask(variant: BaseVariant, results: CompressionResults): Task {
        val aapt2 = variant.project.aapt2Enabled
        val install = variant.createCompressionToolIfNotExists()
        return variant.project.tasks.create("compress${variant.name.capitalize()}ResourcesWith${cmdline.name.substringBefore('.').capitalize()}", getCompressionTaskClass(aapt2).java) {
            it.outputs.upToDateWhen { false }
            it.cmdline = cmdline
            it.variant = variant
            it.results = results
            it.sources = { variant.scope.mergedRes.search(if (aapt2) ::isFlatPng else ::isPng) }
        }.apply {
            dependsOn(install, variant.mergeResourcesTask)
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
