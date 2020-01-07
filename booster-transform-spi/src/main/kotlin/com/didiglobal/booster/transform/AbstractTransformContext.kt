package com.didiglobal.booster.transform

import java.io.File

abstract class AbstractTransformContext(
        override val applicationId: String,
        override val bootClasspath: Collection<File>,
        override val compileClasspath: Collection<File>,
        override val runtimeClasspath: Collection<File>
) : TransformContext {

    override val name: String
        get() = projectDir.name

    override val projectDir: File
        get() = File(System.getProperty("user.dir"))

    override val buildDir: File
        get() = File(projectDir, "build")

    override val temporaryDir: File
        get() = File(buildDir, "temp")

    override val artifacts: ArtifactManager
        get() = object : ArtifactManager {}

    override val klassPool: KlassPool
        get() = object : AbstractKlassPool(runtimeClasspath) {}

    override val originalApplicationId: String
        get() = applicationId

    override val isDebuggable: Boolean
        get() = true

    override fun getProperty(name: String): String? = null

}