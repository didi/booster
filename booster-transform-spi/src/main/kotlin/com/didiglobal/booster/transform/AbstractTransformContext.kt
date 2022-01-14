package com.didiglobal.booster.transform

import java.io.File
import java.util.concurrent.CopyOnWriteArrayList

abstract class AbstractTransformContext(
        final override val applicationId: String,
        final override val name: String,
        final override val bootClasspath: Collection<File>,
        final override val compileClasspath: Collection<File> = emptyList(),
        final override val runtimeClasspath: Collection<File> = emptyList(),
        val bootKlassPool: KlassPool = makeKlassPool(bootClasspath)
) : TransformContext {

    val collectors = CopyOnWriteArrayList<Collector<*>>()

    override val projectDir = File(System.getProperty("user.dir"))

    override val buildDir: File
        get() = File(projectDir, "build")

    override val reportsDir: File
        get() = File(buildDir, "reports")

    override val temporaryDir: File
        get() = File(buildDir, "temp")

    override val artifacts = object : ArtifactManager {}

    override val dependencies: Collection<String> by lazy {
        compileClasspath.map { it.canonicalPath }
    }

    override val klassPool = object : AbstractKlassPool(runtimeClasspath, bootKlassPool) {}

    override val originalApplicationId = applicationId

    override val isDebuggable = true

    override val isDataBindingEnabled = false

    override fun hasProperty(name: String) = false

    override fun <R> registerCollector(collector: Collector<R>) {
        this.collectors += collector
    }

    override fun <R> unregisterCollector(collector: Collector<R>) {
        this.collectors -= collector
    }

}

private fun makeKlassPool(bootClasspath: Collection<File>): KlassPool {
    return when {
        bootClasspath.isEmpty() -> object : AbstractKlassPool(bootClasspath) {}
        else -> object : AbstractKlassPool(bootClasspath) {}
    }
}
