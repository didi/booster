package com.didiglobal.booster.transform

import java.io.File
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

/**
 * Represent the transform context
 *
 * @author johnsonlee
 */
interface TransformContext {

    /**
     * The name of transform
     */
    val name: String

    /**
     * The project directory
     */
    val projectDir: File

    /**
     * The build directory
     */
    val buildDir: File
        get() = File(projectDir, "build")

    /**
     * The temporary directory
     */
    val temporaryDir: File

    /**
     * The reports directory
     */
    val reportsDir: File
        get() = File(buildDir, "reports").also { it.mkdirs() }

    /**
     * The executor service
     */
    val executor: ExecutorService
        get() = Executors.newWorkStealingPool()

    /**
     * The boot classpath
     */
    val bootClasspath: Collection<File>

    /**
     * The compile classpath
     */
    val compileClasspath: Collection<File>

    /**
     * The runtime classpath
     */
    val runtimeClasspath: Collection<File>

    /**
     * The artifact manager
     */
    val artifacts: ArtifactManager

    /**
     * The class pool
     */
    val klassPool: KlassPool
        get() = object : AbstractKlassPool(runtimeClasspath) {}

    /**
     * The application identifier
     */
    val applicationId: String

    /**
     * The original application ID before any overrides from flavors
     */
    val originalApplicationId: String

    /**
     * The buildType is debuggable
     */
    val isDebuggable: Boolean

    /**
     * Check if has the specified property. Generally, the property is equivalent to project property
     *
     * @param name the name of property
     */
    fun hasProperty(name: String) = null != getProperty(name)

    /**
     * Returns the value of the specified property. Generally, the property is equivalent to project property
     *
     * @param name the name of property
     */
    fun getProperty(name: String): String?

}
