package com.didiglobal.booster.transform

import java.io.File

abstract class AbstractTransformContext(
        final override val applicationId: String,
        final override val bootClasspath: Collection<File>,
        final override val compileClasspath: Collection<File>,
        final override val runtimeClasspath: Collection<File>
) : TransformContext {

    override val projectDir = File(System.getProperty("user.dir"))

    override val name: String
        get() = this.projectDir.name

    override val buildDir: File
        get() = File(projectDir, "build")

    override val temporaryDir: File
        get() = File(buildDir, "temp")

    override val artifacts = object : ArtifactManager {}

    override val klassPool = object : AbstractKlassPool(runtimeClasspath) {}

    override val originalApplicationId = applicationId

    override val isDebuggable = true

    override fun getProperty(name: String): String? = null

}