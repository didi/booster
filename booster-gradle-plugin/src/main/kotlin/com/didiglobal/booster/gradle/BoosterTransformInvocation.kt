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
import com.didiglobal.booster.transform.ArtifactManager
import com.didiglobal.booster.transform.Klass
import com.didiglobal.booster.transform.KlassPool
import com.didiglobal.booster.transform.TransformContext
import com.didiglobal.booster.transform.TransformListener
import com.didiglobal.booster.transform.Transformer
import com.didiglobal.booster.transform.util.transform
import com.didiglobal.booster.util.search
import java.io.File
import java.net.URLClassLoader
import java.util.ServiceLoader
import java.util.concurrent.ForkJoinPool

/**
 * Represents a delegate of TransformInvocation
 *
 * @author johnsonlee
 */
internal class BoosterTransformInvocation(private val delegate: TransformInvocation) : TransformInvocation, TransformContext, TransformListener, ArtifactManager {

    /*
     * Preload transformers as List to fix NoSuchElementException caused by ServiceLoader in parallel mode
     */
    private val transformers = ServiceLoader.load(Transformer::class.java, javaClass.classLoader).toList()

    override val name: String = delegate.context.variantName

    override val projectDir: File = delegate.project.projectDir

    override val buildDir: File = delegate.project.buildDir

    override val temporaryDir: File = delegate.context.temporaryDir

    override val reportsDir: File = File(buildDir, "reports").also { it.mkdirs() }

    override val executor = ForkJoinPool(Runtime.getRuntime().availableProcessors(), ForkJoinPool.defaultForkJoinWorkerThreadFactory, null, true)

    override val bootClasspath = delegate.bootClasspath

    override val compileClasspath = delegate.compileClasspath

    override val runtimeClasspath = delegate.runtimeClasspath

    override val artifacts = this

    override val klassPool = KlassPoolImpl(runtimeClasspath)

    override val applicationId = delegate.applicationId

    override val originalApplicationId = delegate.originalApplicationId

    override val isDebuggable = delegate.variant.buildType.isDebuggable

    override fun hasProperty(name: String): Boolean {
        return project.hasProperty(name)
    }

    override fun getProperty(name: String): String? {
        return project.properties[name]?.toString()
    }

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
        ArtifactManager.AAR                           -> variant.scope.getArtifactCollection(RUNTIME_CLASSPATH, ALL, AAR).artifactFiles.files
        ArtifactManager.ALL_CLASSES                   -> variant.scope.allClasses
        ArtifactManager.APK                           -> variant.scope.apk
        ArtifactManager.JAR                           -> variant.scope.getArtifactCollection(RUNTIME_CLASSPATH, ALL, JAR).artifactFiles.files
        ArtifactManager.JAVAC                         -> variant.scope.javac
        ArtifactManager.MERGED_ASSETS                 -> variant.scope.mergedAssets
        ArtifactManager.MERGED_RES                    -> variant.scope.mergedRes
        ArtifactManager.MERGED_MANIFESTS              -> variant.scope.mergedManifests.search { SdkConstants.FN_ANDROID_MANIFEST_XML == it.name }
        ArtifactManager.PROCESSED_RES                 -> variant.scope.processedRes.search { it.name.startsWith(SdkConstants.FN_RES_BASE) && it.name.endsWith(SdkConstants.EXT_RES) }
        ArtifactManager.SYMBOL_LIST                   -> variant.scope.symbolList
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
                    it.forEach { file, status ->
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

    internal class KlassPoolImpl(private val classpath: Collection<File>) : KlassPool {

        private val classLoader = URLClassLoader(classpath.map { it.toURI().toURL() }.toTypedArray())

        private val klasses = mutableMapOf<String, Klass>()

        override fun get(type: String): Klass {
            val name = normalize(type)
            return klasses.getOrDefault(name, findClass(name))
        }

        internal fun findClass(name: String): Klass {
            return try {
                LoadedKlass(this, Class.forName(name, false, classLoader)).also {
                    klasses[name] = it
                }
            } catch (e: Throwable) {
                DefaultKlass(name)
            }
        }

        override fun toString(): String {
            return "classpath: $classpath"
        }

    }

    internal class DefaultKlass(name: String) : Klass {

        override val qualifiedName: String = name

        override fun isAssignableFrom(type: String) = false

        override fun isAssignableFrom(klass: Klass) = klass.qualifiedName == this.qualifiedName

    }

    internal class LoadedKlass(val pool: KlassPoolImpl, val clazz: Class<out Any>) : Klass {

        override val qualifiedName: String = clazz.name

        override fun isAssignableFrom(type: String) = isAssignableFrom(pool.findClass(normalize(type)))

        override fun isAssignableFrom(klass: Klass) = klass is LoadedKlass && clazz.isAssignableFrom(klass.clazz)

    }

}

private fun normalize(type: String) = if (type.contains('/')) {
    type.replace('/', '.')
} else {
    type
}
