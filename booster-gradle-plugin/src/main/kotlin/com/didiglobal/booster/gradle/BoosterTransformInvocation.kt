package com.didiglobal.booster.gradle

import com.android.SdkConstants
import com.android.build.api.transform.Context
import com.android.build.api.transform.DirectoryInput
import com.android.build.api.transform.Format
import com.android.build.api.transform.QualifiedContent
import com.android.build.api.transform.SecondaryInput
import com.android.build.api.transform.Status.ADDED
import com.android.build.api.transform.Status.CHANGED
import com.android.build.api.transform.Status.NOTCHANGED
import com.android.build.api.transform.Status.REMOVED
import com.android.build.api.transform.TransformInput
import com.android.build.api.transform.TransformInvocation
import com.android.build.api.transform.TransformOutputProvider
import com.android.build.gradle.internal.publishing.AndroidArtifacts.ArtifactScope.ALL
import com.android.build.gradle.internal.publishing.AndroidArtifacts.ArtifactType.AAR
import com.android.build.gradle.internal.publishing.AndroidArtifacts.ArtifactType.JAR
import com.android.build.gradle.internal.publishing.AndroidArtifacts.ConsumedConfigType.RUNTIME_CLASSPATH
import com.didiglobal.booster.kotlinx.NCPU
import com.didiglobal.booster.kotlinx.ifNotEmpty
import com.didiglobal.booster.transform.AbstractKlassPool
import com.didiglobal.booster.transform.ArtifactManager
import com.didiglobal.booster.transform.TransformContext
import com.didiglobal.booster.transform.TransformListener
import com.didiglobal.booster.transform.util.transform
import com.didiglobal.booster.util.search
import java.io.File
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

/**
 * Represents a delegate of TransformInvocation
 *
 * @author johnsonlee
 */
internal class BoosterTransformInvocation(private val delegate: TransformInvocation, internal val transform: BoosterTransform, override val executor: ExecutorService = Executors.newWorkStealingPool(NCPU)) : TransformInvocation, TransformContext, TransformListener, ArtifactManager {

    override val name: String = delegate.context.variantName

    override val projectDir: File = delegate.project.projectDir

    override val buildDir: File = delegate.project.buildDir

    override val temporaryDir: File = delegate.context.temporaryDir

    override val reportsDir: File = File(buildDir, "reports").also { it.mkdirs() }

    override val bootClasspath = delegate.bootClasspath

    override val compileClasspath = delegate.compileClasspath

    override val runtimeClasspath = delegate.runtimeClasspath

    override val artifacts = this

    override val klassPool: AbstractKlassPool = object : AbstractKlassPool(compileClasspath, transform.bootKlassPool) {}

    override val applicationId = delegate.applicationId

    override val originalApplicationId = delegate.originalApplicationId

    override val isDebuggable = delegate.variant.buildType.isDebuggable

    override val isDataBindingEnabled = delegate.isDataBindingEnabled

    override fun hasProperty(name: String) = project.hasProperty(name)

    override fun getProperty(name: String): String? = project.properties[name]?.toString()

    override fun getInputs(): MutableCollection<TransformInput> = delegate.inputs

    override fun getSecondaryInputs(): MutableCollection<SecondaryInput> = delegate.secondaryInputs

    override fun getReferencedInputs(): MutableCollection<TransformInput> = delegate.referencedInputs

    override fun isIncremental() = delegate.isIncremental

    override fun getOutputProvider(): TransformOutputProvider? = delegate.outputProvider

    override fun getContext(): Context = delegate.context

    override fun onPreTransform(context: TransformContext) = transform.transformers.forEach {
        it.onPreTransform(this)
    }

    override fun onPostTransform(context: TransformContext) = transform.transformers.forEach {
        it.onPostTransform(this)
    }

    override fun get(type: String): Collection<File> = when (type) {
        ArtifactManager.AAR -> variant.scope.getArtifactCollection(RUNTIME_CLASSPATH, ALL, AAR).artifactFiles.files
        ArtifactManager.ALL_CLASSES -> variant.scope.allClasses
        ArtifactManager.APK -> variant.scope.apk
        ArtifactManager.JAR -> variant.scope.getArtifactCollection(RUNTIME_CLASSPATH, ALL, JAR).artifactFiles.files
        ArtifactManager.JAVAC -> variant.scope.javac
        ArtifactManager.MERGED_ASSETS -> variant.scope.mergedAssets
        ArtifactManager.MERGED_RES -> variant.scope.mergedRes
        ArtifactManager.MERGED_MANIFESTS -> variant.scope.mergedManifests.search { SdkConstants.FN_ANDROID_MANIFEST_XML == it.name }
        ArtifactManager.PROCESSED_RES -> variant.scope.processedRes.search { it.name.startsWith(SdkConstants.FN_RES_BASE) && it.name.endsWith(SdkConstants.EXT_RES) }
        ArtifactManager.SYMBOL_LIST -> variant.scope.symbolList
        ArtifactManager.SYMBOL_LIST_WITH_PACKAGE_NAME -> variant.scope.symbolListWithPackageName
        ArtifactManager.DATA_BINDING_DEPENDENCY_ARTIFACTS -> variant.scope.dataBindingDependencyArtifacts.listFiles()?.toList() ?: emptyList()
        else -> TODO("Unexpected type: $type")
    }

    internal fun doFullTransform() {
        this.inputs.map {
            it.jarInputs + it.directoryInputs
        }.flatten().forEach { input ->
            executor.execute {
                val format = if (input is DirectoryInput) Format.DIRECTORY else Format.JAR
                outputProvider?.let { provider ->
                    project.logger.info("Transforming ${input.file}")
                    input.transform(provider.getContentLocation(input.name, input.contentTypes, input.scopes, format), this)
                }
            }
        }
    }

    @Suppress("NON_EXHAUSTIVE_WHEN")
    internal fun doIncrementalTransform() {
        this.inputs.parallelStream().forEach { input ->
            input.jarInputs.parallelStream().filter { it.status != NOTCHANGED }.forEach { jarInput ->
                when (jarInput.status) {
                    REMOVED -> jarInput.file.delete()
                    CHANGED, ADDED -> {
                        project.logger.info("Transforming ${jarInput.file}")
                        outputProvider?.let { provider ->
                            jarInput.transform(provider.getContentLocation(jarInput.name, jarInput.contentTypes, jarInput.scopes, Format.JAR), this)
                        }
                    }
                }
            }

            input.directoryInputs.parallelStream().forEach { dirInput ->
                val base = dirInput.file.toURI()
                dirInput.changedFiles.ifNotEmpty {
                    it.forEach { (file, status) ->
                        when (status) {
                            REMOVED -> file.delete()
                            ADDED, CHANGED -> {
                                project.logger.info("Transforming $file")
                                outputProvider?.let { provider ->
                                    val root = provider.getContentLocation(dirInput.name, dirInput.contentTypes, dirInput.scopes, Format.DIRECTORY)
                                    val output = File(root, base.relativize(file.toURI()).path)
                                    file.transform(output) { bytecode ->
                                        bytecode.transform(this)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

}

private fun ByteArray.transform(invocation: BoosterTransformInvocation): ByteArray {
    return invocation.transform.transformers.fold(this) { bytes, transformer ->
        transformer.transform(invocation, bytes)
    }
}

private fun QualifiedContent.transform(output: File, invocation: BoosterTransformInvocation) {
    this.file.transform(output) { bytecode ->
        bytecode.transform(invocation)
    }
}
