package com.didiglobal.booster.transform

import java.io.File
import java.util.concurrent.ExecutorService

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

    /**
     * The temporary directory
     */
    val temporaryDir: File

    /**
     * The executor service
     */
    val executor: ExecutorService

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

    /**
     * Check if has the specified property. Generally, the property is equivalent to project property
     *
     * @param name the name of property
     */
    fun hasProperty(name: String): Boolean

    /**
     * Returns the value of the specified property. Generally, the property is equivalent to project property
     *
     * @param name the name of property
     */
    fun getProperty(name: String): String?

}
