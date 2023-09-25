package com.didiglobal.booster.compression

import com.android.build.api.variant.Variant
import com.android.build.gradle.internal.tasks.factory.dependsOn
import com.didiglobal.booster.BOOSTER
import com.didiglobal.booster.command.CommandInstaller
import com.didiglobal.booster.compression.task.CompressImages
import com.didiglobal.booster.compression.task.MATCH_ALL_RESOURCES
import com.didiglobal.booster.compression.task.excludes
import com.didiglobal.booster.gradle.*
import com.didiglobal.booster.kotlinx.Wildcard
import com.didiglobal.booster.kotlinx.capitalized
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.UnknownTaskException
import org.gradle.api.tasks.TaskProvider
import java.io.File
import kotlin.reflect.KClass

/**
 * Represents a simple implementation of [CompressionTaskCreator]
 *
 * @author johnsonlee
 */
class SimpleCompressionTaskCreator(private val tool: CompressionTool, private val compressor: (Boolean) -> KClass<out CompressImages<out CompressionOptions>>) : CompressionTaskCreator {

    override fun getCompressionTaskClass(aapt2: Boolean) = compressor(aapt2)

    override fun createCompressionTask(
        variant: Variant,
        results: CompressionResults,
        name: String,
        supplier: () -> Collection<File>,
        ignores: Set<Wildcard>,
        vararg deps: TaskProvider<out Task>
    ): TaskProvider<out CompressImages<out CompressionOptions>> {
        val project = variant.project
        val aapt2 = project.isAapt2Enabled
        val install = getCommandInstaller(variant)

        return project.tasks.register("compress${variant.name.capitalized()}${name.capitalized()}With${tool.command.name.substringBefore('.').capitalized()}", getCompressionTaskClass(aapt2).java) { task ->
            task.group = BOOSTER
            task.description = "Compress image resources by ${tool.command.name} for ${variant.name}"
            task.dependsOn(variant.preBuildTaskProvider.get())
            task.tool = tool
            task.variant = variant
            task.results = results
            task.filter = if (ignores.isNotEmpty()) excludes(ignores) else MATCH_ALL_RESOURCES
            task.images = lazy(supplier)::value
        }.apply {
            dependsOn(install)
            deps.forEach { dependsOn(it) }
            variant.processResTaskProvider?.dependsOn(this)
            variant.bundleResourcesTaskProvider?.dependsOn(this)
        }
    }

    private fun getCommandInstaller(variant: Variant): TaskProvider<out Task> {
        return variant.project.tasks.register(getInstallTaskName(variant.name)) {
            it.group = BOOSTER
            it.description = "Install ${tool.command.name} for ${variant.name}"
        }.apply {
            dependsOn(getCommandInstaller(variant.project))
            dependsOn(variant.mergeResourcesTaskProvider)
        }
    }

    private fun getCommandInstaller(project: Project): TaskProvider<out Task> {
        val name = getInstallTaskName()
        return try {
            project.tasks.named(name)
        } catch (e: UnknownTaskException) {
            null
        } ?: project.tasks.register(name, CommandInstaller::class.java) {
            it.group = BOOSTER
            it.description = "Install ${tool.command.name}"
            it.command = tool.command
        }
    }

    private fun getInstallTaskName(variant: String = ""): String {
        return "install${variant.capitalized()}${tool.command.name.substringBefore('.').capitalized()}"
    }

}

