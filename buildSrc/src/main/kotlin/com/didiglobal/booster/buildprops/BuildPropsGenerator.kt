package com.didiglobal.booster.buildprops

import org.gradle.api.internal.AbstractTask
import org.gradle.api.plugins.JavaPluginConvention
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.SourceSet
import org.gradle.api.tasks.TaskAction
import java.io.File
import java.io.IOException
import java.util.StringTokenizer

/**
 * The task for `Build.java` source file generating
 *
 * @author johnsonlee
 */
open class BuildPropsGenerator : AbstractTask() {

    val output: File
        @OutputDirectory
        get() = project.getGeneratedSourceDir(project.convention.getPlugin(JavaPluginConvention::class.java).sourceSets.getByName(SourceSet.MAIN_SOURCE_SET_NAME))

    @TaskAction
    @Throws(IOException::class)
    fun run() {
        val pkg = mkpkg("${project.group}.${project.name}")
        val path = "${pkg.replace(".", File.separator)}${File.separator}Build.java"
        val revision = File(project.rootProject.projectDir, ".git${File.separator}logs${File.separator}HEAD").useLines {
            StringTokenizer(it.last()).nextToken(1)
        } ?: ""

        File(output, path).also {
            it.parentFile.mkdirs()
            it.createNewFile()
        }.printWriter().use {
            it.apply {
                it.println("""
                /**
                 * DO NOT MODIFY! This file is generated automatically.
                 */
                package $pkg;

                public interface Build {
                    String GROUP = "${project.group}";
                    String ARTIFACT = "${project.name}";
                    String VERSION = "${project.version}";
                    String REVISION = "$revision";
                }
                """.trimIndent())
            }
        }
    }

}

/**
 * Make a safe package name
 */
internal fun mkpkg(s: String): String {
    return StringBuilder(s.length).apply {
        s.forEachIndexed { i, c ->
            if (0 == i) {
                append(if ('.' != c && !c.isJavaIdentifierStart()) '.' else c)
            } else {
                append(if ('.' != c && !c.isJavaIdentifierPart()) '.' else c)
            }
        }
    }.toString().split('.').let {
        it.filterIndexed { i, s -> !(i > 0 && s == it[i - 1]) }
    }.joinToString(".")
}

internal fun StringTokenizer.nextToken(nth: Int): String? {
    var i = 0

    while (hasMoreTokens()) {
        val token = nextToken()
        if (nth == i++) {
            return token
        }
    }

    return null
}
