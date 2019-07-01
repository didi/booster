package com.didiglobal.booster.task.so

import com.android.build.gradle.api.BaseVariant
import com.didiglobal.booster.gradle.ResolvedArtifactResults
import com.didiglobal.booster.kotlinx.CSI_CYAN
import com.didiglobal.booster.kotlinx.CSI_RESET
import com.didiglobal.booster.kotlinx.ifNotEmpty
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import java.util.jar.JarFile

internal open class ListSharedLibrary : DefaultTask() {

    lateinit var variant: BaseVariant

    @TaskAction
    fun run() {
        ResolvedArtifactResults(variant).forEach { result ->
            when (result.file.extension.toLowerCase()) {
                "aar", "jar" -> {
                    JarFile(result.file).use { jar ->
                        jar.entries().asSequence().filter {
                            it.name.endsWith(".so")
                        }.sortedBy {
                            it.name
                        }.toList().ifNotEmpty { libs ->
                            println("$CSI_CYAN${result.id.componentIdentifier}$CSI_RESET\n${libs.joinToString("\n") {
                                "  - ${it.name}"
                            }}")
                        }
                    }
                }
            }
        }
    }

}
