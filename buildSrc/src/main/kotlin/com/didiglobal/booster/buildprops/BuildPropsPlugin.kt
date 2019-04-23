package com.didiglobal.booster.buildprops

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPluginConvention
import org.gradle.api.tasks.SourceSet
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.api.tasks.SourceTask
import org.gradle.plugins.ide.idea.model.IdeaModel
import org.gradle.plugins.ide.idea.model.IdeaModule
import java.io.File
import java.util.LinkedHashSet

/**
 * Gradle plugin for `Build.java` source code generating.
 *
 * @author johnsonlee
 */
class BuildPropsPlugin : Plugin<Project> {

    override fun apply(project: Project) {
        if (!project.plugins.hasPlugin("idea")) {
            project.plugins.apply("idea")
        }

        val convention = project.convention
        val javaPlugin = convention.getPlugin(JavaPluginConvention::class.java)
        val sourceSets = javaPlugin.sourceSets
        val buildProps = project.tasks.create("generateBuildProps", BuildPropsGenerator::class.java) {
            it.outputs.upToDateWhen { false }
        }

        project.configureIdeaModule(sourceSets)
        sourceSets.filter { it.name == SourceSet.MAIN_SOURCE_SET_NAME }.forEach { sourceSet ->
            (project.tasks.findByName(sourceSet.getCompileTaskName("java"))?.dependsOn(buildProps) as? SourceTask)?.source(buildProps.output)
            (project.tasks.findByName(sourceSet.getCompileTaskName("groovy"))?.dependsOn(buildProps) as? SourceTask)?.source(buildProps.output)
            (project.tasks.findByName(sourceSet.getCompileTaskName("kotlin"))?.dependsOn(buildProps) as? SourceTask)?.source(buildProps.output)
        }
    }

}

internal const val PLUGIN_ID = "buildprops"

internal val GENERATED_SOURCE_ROOT = "generated${File.separator}source${File.separator}$PLUGIN_ID"

internal fun Project.getGeneratedSourceDir(sourceSet: SourceSet) = File(this.buildDir, GENERATED_SOURCE_ROOT + File.separator + sourceSet.name + File.separator + "java")

private fun Project.configureIdeaModule(sourceSets: SourceSetContainer) {
    val mainSourceSet = sourceSets.getByName(SourceSet.MAIN_SOURCE_SET_NAME)
    val testSourceSet = sourceSets.getByName(SourceSet.TEST_SOURCE_SET_NAME)
    val mainGeneratedSourcesDir = getGeneratedSourceDir(mainSourceSet)
    val testGeneratedSourcesDir = getGeneratedSourceDir(testSourceSet)
    val ideaModule = extensions.getByType(IdeaModel::class.java).module
    ideaModule.excludeDirs = getIdeaExcludeDirs(getGeneratedSourceDirs(sourceSets), ideaModule)
    ideaModule.sourceDirs = files(ideaModule.sourceDirs, mainGeneratedSourcesDir).files
    ideaModule.testSourceDirs = files(ideaModule.testSourceDirs, testGeneratedSourcesDir).files
    ideaModule.generatedSourceDirs = files(ideaModule.generatedSourceDirs, mainGeneratedSourcesDir, testGeneratedSourcesDir).files
}

private fun Project.getGeneratedSourceDirs(sourceSets: SourceSetContainer): Set<File> = LinkedHashSet<File>().also { excludes ->
    sourceSets.forEach { sourceSet ->
        var f = getGeneratedSourceDir(sourceSet)

        while (f != this.projectDir) {
            excludes.add(f)
            f = f.parentFile
        }
    }
}

private fun Project.getIdeaExcludeDirs(excludes: Set<File>, ideaModule: IdeaModule): Set<File> = LinkedHashSet(ideaModule.excludeDirs).also { excludeDirs ->
    if (excludes.contains(buildDir) && excludeDirs.contains(buildDir)) {
        excludeDirs.remove(buildDir)
        buildDir.listFiles()?.filter {
            it.isDirectory
        }?.forEach {
            excludeDirs.add(it)
        }
    }

    excludeDirs.removeAll(excludes)
}
