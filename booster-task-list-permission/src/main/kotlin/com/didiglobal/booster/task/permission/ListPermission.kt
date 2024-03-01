package com.didiglobal.booster.task.permission

import com.didiglobal.booster.kotlinx.ifNotEmpty
import org.gradle.api.DefaultTask
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.TaskAction
import javax.xml.parsers.SAXParserFactory

internal abstract class ListPermission : DefaultTask() {

    private val factory = SAXParserFactory.newInstance()

    @get:InputFile
    abstract val mergedManifest: RegularFileProperty

    init {
        factory.isXIncludeAware = false
        factory.isNamespaceAware = true
        factory.setFeature("http://xml.org/sax/features/namespace-prefixes", true)
        factory.setFeature("http://xml.org/sax/features/xmlns-uris", true)
        factory.isValidating = false
    }

    @TaskAction
    fun run() {
        mergedManifest.get().asFile.inputStream().use { source ->
            PermissionUsageHandler().also { handler ->
                factory.newSAXParser().parse(source, handler)
            }.permissions.sorted().ifNotEmpty { permissions ->
                permissions.forEach { permission ->
                    println("  - $permission")
                }
            }
        }
    }

}