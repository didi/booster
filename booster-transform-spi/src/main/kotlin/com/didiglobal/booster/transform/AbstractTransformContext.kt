package com.didiglobal.booster.transform

import java.io.File
import java.net.URLClassLoader
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

abstract class AbstractTransformContext(
        final override val applicationId: String,
        final override val bootClasspath: Collection<File>,
        final override val compileClasspath: Collection<File>,
        final override val runtimeClasspath: Collection<File>,
        final val bootKlassPool: AbstractKlassPool = object : AbstractKlassPool(bootClasspath) {}
) : TransformContext {

    override val projectDir = File(System.getProperty("user.dir"))

    override val name: String
        get() = this.projectDir.name

    override val buildDir: File
        get() = File(projectDir, "build")

    override val reportsDir: File
        get() = File(buildDir, "reports")

    override val temporaryDir: File
        get() = File(buildDir, "temp")

    override val executor: ExecutorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors())

    override val artifacts = object : ArtifactManager {}

    override val klassPool = object : AbstractKlassPool(runtimeClasspath, bootKlassPool) {}

    override val originalApplicationId = applicationId

    override val isDebuggable = true

    override fun getProperty(name: String): String? = null

    override fun hasProperty(name: String) = false

}