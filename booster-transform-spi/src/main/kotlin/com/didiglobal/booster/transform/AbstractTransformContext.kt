package com.didiglobal.booster.transform

import java.io.File

abstract class AbstractTransformContext(
        final override val applicationId: String,
        final override val name: String,
        final override val bootClasspath: Collection<File>,
        final override val compileClasspath: Collection<File> = emptyList(),
        final override val runtimeClasspath: Collection<File> = emptyList(),
        final val bootKlassPool: AbstractKlassPool = object : AbstractKlassPool(bootClasspath) {}
) : TransformContext {

    override val projectDir = File(System.getProperty("user.dir"))

    override val buildDir: File
        get() = File(projectDir, "build")

    override val reportsDir: File
        get() = File(buildDir, "reports")

    override val temporaryDir: File
        get() = File(buildDir, "temp")

    override val artifacts = object : ArtifactManager {
        override fun get(type: String) = when (type) {
            ArtifactManager.MERGED_ASSETS -> setOf(buildDir.getMergedAssets(name))
            ArtifactManager.MERGED_RES -> setOf(buildDir.getMergedRes(name))
            ArtifactManager.MERGED_MANIFESTS -> setOf(buildDir.getMergedManifests(name))
            ArtifactManager.PROCESSED_RES -> setOf(buildDir.getProcessedRes(name))
            else -> TODO("Unsupported artifact type: $type")
        }
    }

    override val klassPool = object : AbstractKlassPool(runtimeClasspath, bootKlassPool) {}

    override val originalApplicationId = applicationId

    override val isDebuggable = true

    override val isDataBindingEnabled = false

    override fun hasProperty(name: String) = false

}

internal fun File.getMergedAssets(variant: String) = File(this, arrayOf("intermediates", "merged_assets", variant, "out").joinToString(File.separator))

internal fun File.getMergedRes(variant: String) = File(this, arrayOf("intermediates", "res", "merged", variant).joinToString(File.separator))

internal fun File.getMergedManifests(variant: String) = File(this, arrayOf("intermediates", "merged_manifests", variant, "AndroidManifest.xml").joinToString(File.separator))

private fun File.getProcessedRes(variant: String) = File(this, arrayOf("intermediates", "processed_res", variant, "out", "resources-${variant}.ap_").joinToString(File.separator))
