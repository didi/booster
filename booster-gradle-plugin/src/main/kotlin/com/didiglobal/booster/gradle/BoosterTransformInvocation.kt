package com.didiglobal.booster.gradle

import com.android.SdkConstants
import com.android.build.api.transform.Context
import com.android.build.api.transform.Format
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
import com.didiglobal.booster.kotlinx.ifNotEmpty
import com.didiglobal.booster.transform.AbstractKlassPool
import com.didiglobal.booster.transform.ArtifactManager
import com.didiglobal.booster.transform.TransformContext
import com.didiglobal.booster.transform.TransformListener
import com.didiglobal.booster.transform.Transformer
import com.didiglobal.booster.transform.util.transform
import com.didiglobal.booster.util.search
import java.io.File
import java.util.ServiceLoader

/**
 * Represents a delegate of TransformInvocation
 *
 * @author johnsonlee
 */
internal class BoosterTransformInvocation(private val delegate: TransformInvocation, val transform: BoosterTransform) : TransformInvocation, TransformContext, TransformListener, ArtifactManager {

    /*
     * Preload transformers as List to fix NoSuchElementException caused by ServiceLoader in parallel mode
     */
    private val transformers = ServiceLoader.load(Transformer::class.java, javaClass.classLoader).toList()

    override val name: String = delegate.context.variantName

    override val projectDir: File = delegate.project.projectDir

    override val buildDir: File = delegate.project.buildDir

    override val temporaryDir: File = delegate.context.temporaryDir

    override val reportsDir: File = File(buildDir, "reports").also { it.mkdirs() }

    override val executor = transform.executor

    override val bootClasspath = delegate.bootClasspath

    override val compileClasspath = delegate.compileClasspath

    override val runtimeClasspath = delegate.runtimeClasspath

    override val artifacts = this

    override val klassPool: AbstractKlassPool = object : AbstractKlassPool(compileClasspath, transform.bootKlassPool) {}

    override val applicationId = delegate.applicationId

    override val originalApplicationId = delegate.originalApplicationId

    override val isDebuggable = delegate.variant.buildType.isDebuggable

    override fun hasProperty(name: String) = project.hasProperty(name)

    override fun getProperty(name: String): String? = project.properties[name]?.toString()

    override fun getInputs(): MutableCollection<TransformInput> = delegate.inputs

    override fun getSecondaryInputs(): MutableCollection<SecondaryInput> = delegate.secondaryInputs

    override fun getReferencedInputs(): MutableCollection<TransformInput> = delegate.referencedInputs

    override fun isIncremental() = delegate.isIncremental

    override fun getOutputProvider(): TransformOutputProvider = delegate.outputProvider

    override fun getContext(): Context = delegate.context

    override fun onPreTransform(context: TransformContext) = transformers.forEach {
        it.onPreTransform(this)
    }

    override fun onPostTransform(context: TransformContext) = transformers.forEach {
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
        else -> TODO("Unexpected type: $type")
    }

    internal fun doFullTransform() {
        this.inputs.parallelStream().forEach { input ->
            input.directoryInputs.parallelStream().forEach {
                project.logger.info("Transforming ${it.file}")
                it.file.transform(outputProvider.getContentLocation(it.name, it.contentTypes, it.scopes, Format.DIRECTORY)) { bytecode ->
                    bytecode.transform(this)
                }
            }
            input.jarInputs.parallelStream().forEach {
                project.logger.info("Transforming ${it.file}")
                it.file.transform(outputProvider.getContentLocation(it.name, it.contentTypes, it.scopes, Format.JAR)) { bytecode ->
                    bytecode.transform(this)
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
                        val root = outputProvider.getContentLocation(jarInput.name, jarInput.contentTypes, jarInput.scopes, Format.JAR)
                        project.logger.info("Transforming ${jarInput.file}")
                        jarInput.file.transform(root) { bytecode ->
                            bytecode.transform(this)
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
                                val root = outputProvider.getContentLocation(dirInput.name, dirInput.contentTypes, dirInput.scopes, Format.DIRECTORY)
                                project.logger.info("Transforming $file")
                                file.transform(File(root, base.relativize(file.toURI()).path)) { bytecode ->
                                    bytecode.transform(this)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private fun ByteArray.transform(invocation: BoosterTransformInvocation): ByteArray {
        return transformers.fold(this) { bytes, transformer ->
            transformer.transform(invocation, bytes)
        }
    }

}
