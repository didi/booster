package com.didiglobal.booster.gradle

import com.android.build.api.instrumentation.ClassContext
import com.didiglobal.booster.transform.AbstractKlassPool
import com.didiglobal.booster.transform.AbstractTransformContext
import com.didiglobal.booster.transform.ArtifactManager
import com.didiglobal.booster.transform.Klass
import com.didiglobal.booster.transform.KlassPool
import com.didiglobal.booster.transform.Transformer
import org.gradle.api.file.RegularFile
import org.gradle.api.logging.Logging
import org.gradle.api.services.BuildService
import org.gradle.api.services.BuildServiceParameters
import java.io.File
import java.util.concurrent.ConcurrentHashMap

/**
 * Holds the transformation state per variant across the project classes instrumentation task
 * and the dependency instrumentation transforms, and drives the transformer lifecycle:
 *
 * - [Transformer.onPreTransform] is called once before the first class of the variant is transformed
 * - [Transformer.onPostTransform] is called once when the build finishes
 *
 * @author johnsonlee
 */
abstract class BoosterInstrumentationService : BuildService<BuildServiceParameters.None>, AutoCloseable {

    private val units = ConcurrentHashMap<String, TransformUnit>()

    /**
     * Registers the transformers of the specified variant at configuration time
     */
    internal fun register(projectPath: String, variantName: String, transformers: Collection<Transformer>) {
        units[keyOf(projectPath, variantName)] = TransformUnit(transformers)
    }

    internal fun unitOf(params: BoosterInstrumentationParameters): TransformUnit {
        return units.computeIfAbsent(keyOf(params.projectPath.get(), params.variantName.get())) {
            // the configuration phase has been skipped, e.g. the configuration cache is reused
            logger.info("Loading transformers for $it from ${javaClass.classLoader}")
            TransformUnit(loadTransformers(BoosterInstrumentationService::class.java.classLoader))
        }
    }

    override fun close() {
        units.values.forEach(TransformUnit::close)
        units.clear()
    }

    companion object {

        internal fun keyOf(projectPath: String, variantName: String) = "$projectPath:$variantName"

        internal fun nameOf(projectPath: String) = "${BoosterInstrumentationService::class.java.name}$projectPath"

    }

}

/**
 * The transformers and the lazily created [InstrumentationTransformContext] of a variant
 */
internal class TransformUnit(private val transformers: Collection<Transformer>) : AutoCloseable {

    @Volatile
    private var context: InstrumentationTransformContext? = null

    private fun prepare(params: BoosterInstrumentationParameters): InstrumentationTransformContext {
        return context ?: synchronized(this) {
            context ?: InstrumentationTransformContext(params).also { ctx ->
                transformers.forEach {
                    it.onPreTransform(ctx)
                }
                context = ctx
            }
        }
    }

    fun transform(params: BoosterInstrumentationParameters, classContext: ClassContext, bytecode: ByteArray): ByteArray {
        val ctx = prepare(params)
        return ctx.withClassContext(classContext) {
            transformers.fold(bytecode) { bytes, transformer ->
                transformer.transform(ctx, bytes)
            }
        }
    }

    override fun close() {
        val ctx = synchronized(this) {
            context.also {
                context = null
            }
        } ?: return
        try {
            transformers.forEach {
                it.onPostTransform(ctx)
            }
        } finally {
            ctx.close()
        }
    }

}

/**
 * The [com.didiglobal.booster.transform.TransformContext] backed by [BoosterInstrumentationParameters]
 */
internal class InstrumentationTransformContext(params: BoosterInstrumentationParameters) : AbstractTransformContext(
    params.applicationId.get(),
    params.variantName.get(),
    params.bootClasspath.get().map(RegularFile::getAsFile)
), AutoCloseable {

    override val projectDir: File = params.projectDir.get().asFile

    override val isDebuggable: Boolean = params.debuggable.get()

    override val isDataBindingEnabled: Boolean = params.dataBindingEnabled.get()

    override val dependencies: Collection<String> = params.dependencies.get()

    private val properties: Map<String, String> = params.properties.get()

    /**
     * Only the artifacts which are declared as inputs of instrumentation are available,
     * the others are either produced after instrumentation (e.g. APK) or too expensive to be inputs (e.g. merged resources)
     */
    private val artifactFiles: Map<String, List<File>> = mapOf(
        ArtifactManager.MERGED_MANIFESTS to params.mergedManifests.files.flatMap { it.manifests() },
        ArtifactManager.SYMBOL_LIST to params.symbolList.files.toList()
    )

    override val artifacts: ArtifactManager = object : ArtifactManager {
        override fun get(type: String): Collection<File> = artifactFiles[type] ?: emptyList<File>().also {
            logger.warn("Artifact $type is not available in $TRANSFORM_MODE_INSTRUMENTATION mode, set $PROPERTY_TRANSFORM_MODE=$TRANSFORM_MODE_SCOPED_ARTIFACTS to access it")
        }
    }

    private val currentClassContext = ThreadLocal<ClassContext>()

    override val klassPool: AbstractKlassPool = ClassContextKlassPool(bootKlassPool) {
        currentClassContext.get()
    }

    override fun hasProperty(name: String): Boolean = properties.containsKey(name)

    @Suppress("UNCHECKED_CAST")
    override fun <T> getProperty(name: String, default: T): T {
        val value = properties[name] ?: return default
        return when (default) {
            is Boolean -> value.toBoolean()
            is Byte -> value.toByte()
            is Short -> value.toShort()
            is Int -> value.toInt()
            is Float -> value.toFloat()
            is Long -> value.toLong()
            is Double -> value.toDouble()
            else -> value
        } as T
    }

    fun <R> withClassContext(classContext: ClassContext, action: () -> R): R {
        val previous = currentClassContext.get()
        currentClassContext.set(classContext)
        try {
            return action()
        } finally {
            if (previous == null) {
                currentClassContext.remove()
            } else {
                currentClassContext.set(previous)
            }
        }
    }

    override fun close() {
        klassPool.close()
        bootKlassPool.close()
    }

}

private val logger = Logging.getLogger(InstrumentationTransformContext::class.java)

private const val ANDROID_MANIFEST_XML = "AndroidManifest.xml"

private fun File.manifests(): List<File> = when {
    isDirectory -> walkTopDown().filter { it.isFile && it.name == ANDROID_MANIFEST_XML }.toList()
    else -> listOf(this)
}

/**
 * The [KlassPool] which resolves the class hierarchy from AGP's [ClassContext] instead of loading classes,
 * falls back to the [parent] (the boot class pool) when no [ClassContext] is available,
 * e.g. in [Transformer.onPreTransform].
 */
internal class ClassContextKlassPool(
    parent: KlassPool,
    private val classContext: () -> ClassContext?
) : AbstractKlassPool(emptyList(), parent) {

    override fun get(type: String): Klass = ClassContextKlass(type.toQualifiedName())

    private inner class ClassContextKlass(override val qualifiedName: String) : Klass {

        override fun isAssignableFrom(type: String): Boolean {
            val name = type.toQualifiedName()
            if (name == qualifiedName) {
                return true
            }
            val data = classContext()?.loadClassData(name) ?: return parent!![qualifiedName].isAssignableFrom(name)
            return data.superClasses.contains(qualifiedName) || data.interfaces.contains(qualifiedName)
        }

        override fun isAssignableFrom(klass: Klass): Boolean = isAssignableFrom(klass.qualifiedName)

        override fun toString(): String = qualifiedName

    }

}

private fun String.toQualifiedName() = replace('/', '.')
