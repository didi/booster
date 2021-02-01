package com.didiglobal.booster.compression

import com.android.build.gradle.api.BaseVariant
import com.didiglobal.booster.command.CommandInstaller
import com.didiglobal.booster.compression.task.CompressImages
import com.didiglobal.booster.compression.task.MATCH_ALL_RESOURCES
import com.didiglobal.booster.compression.task.excludes
import com.didiglobal.booster.gradle.aapt2Enabled
import com.didiglobal.booster.gradle.bundleResourcesTask
import com.didiglobal.booster.gradle.mergeResourcesTask
import com.didiglobal.booster.gradle.processResTask
import com.didiglobal.booster.gradle.project
import com.didiglobal.booster.kotlinx.Wildcard
import org.gradle.api.Task
import java.io.File
import kotlin.reflect.KClass

/**
 * Represents a simple implementation of [CompressionTaskCreator]
 *
 * @author johnsonlee
 */
class SimpleCompressionTaskCreator(private val tool: CompressionTool, private val compressor: (Boolean) -> KClass<out CompressImages<out CompressionOptions>>) : CompressionTaskCreator {

    override fun getCompressionTaskClass(aapt2: Boolean) = compressor(aapt2)

    override fun createCompressionTask(variant: BaseVariant, results: CompressionResults, name: String, supplier: () -> Collection<File>, ignores: Set<Wildcard>, vararg deps: Task): CompressImages<out CompressionOptions> {
        val aapt2 = variant.project.aapt2Enabled
        val install = getCommandInstaller(variant)

        return variant.project.tasks.create("compress${variant.name.capitalize()}${name.capitalize()}With${tool.command.name.substringBefore('.').capitalize()}", getCompressionTaskClass(aapt2).java) { task ->
            task.tool = tool
            task.variant = variant
            task.results = results
            task.filter = if (ignores.isNotEmpty()) excludes(ignores) else MATCH_ALL_RESOURCES
            task.supplier = {
                supplier().filter { it.length() > 0 }.sortedBy { it }
            }
        }.apply {
            dependsOn(install, deps)
            variant.processResTask?.dependsOn(this)
            variant.bundleResourcesTask?.dependsOn(this)
        }
    }

    private fun getCommandInstaller(variant: BaseVariant): Task {
        val name = "install${tool.command.name.substringBefore('.').capitalize()}"
        return variant.project.tasks.findByName(name) ?: variant.project.tasks.create(name, CommandInstaller::class.java) {
            it.command = tool.command
        }.dependsOn(variant.mergeResourcesTask)
    }

}

