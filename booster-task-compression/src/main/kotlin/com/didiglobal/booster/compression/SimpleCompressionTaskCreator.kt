package com.didiglobal.booster.compression

import com.android.build.gradle.api.BaseVariant
import com.android.build.gradle.internal.publishing.AndroidArtifacts
import com.android.build.gradle.internal.tasks.factory.dependsOn
import com.didiglobal.booster.command.CommandInstaller
import com.didiglobal.booster.compression.task.CompressImages
import com.didiglobal.booster.compression.task.MATCH_ALL_RESOURCES
import com.didiglobal.booster.compression.task.excludes
import com.didiglobal.booster.gradle.aapt2Enabled
import com.didiglobal.booster.gradle.bundleResourcesTaskProvider
import com.didiglobal.booster.gradle.getArtifactFileCollection
import com.didiglobal.booster.gradle.isAapt2Enabled
import com.didiglobal.booster.gradle.mergeResourcesTask
import com.didiglobal.booster.gradle.mergeResourcesTaskProvider
import com.didiglobal.booster.gradle.preBuildTaskProvider
import com.didiglobal.booster.gradle.processResTaskProvider
import com.didiglobal.booster.gradle.project
import com.didiglobal.booster.kotlinx.Wildcard
import com.didiglobal.booster.kotlinx.search
import org.gradle.api.Task
import org.gradle.api.UnknownTaskException
import org.gradle.api.tasks.TaskProvider
import org.gradle.internal.concurrent.GradleThread
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
            variant: BaseVariant,
            results: CompressionResults,
            name: String,
            supplier: () -> Collection<File>,
            ignores: Set<Wildcard>,
            vararg deps: TaskProvider<out Task>
    ): TaskProvider<out CompressImages<out CompressionOptions>> {
        val project = variant.project
        val aapt2 = project.isAapt2Enabled
        val install = getCommandInstaller(variant)

        return project.tasks.register("compress${variant.name.capitalize()}${name.capitalize()}With${tool.command.name.substringBefore('.').capitalize()}", getCompressionTaskClass(aapt2).java) { task ->
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

    private fun getCommandInstaller(variant: BaseVariant): TaskProvider<out Task> {
        val name = "install${tool.command.name.substringBefore('.').capitalize()}"
        return (try {
            variant.project.tasks.named(name)
        } catch (e: UnknownTaskException) {
            null
        } ?: variant.project.tasks.register(name, CommandInstaller::class.java) {
            it.command = tool.command
        }).dependsOn(variant.mergeResourcesTaskProvider)
    }

}

