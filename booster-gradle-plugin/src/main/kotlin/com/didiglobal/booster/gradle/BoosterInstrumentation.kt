package com.didiglobal.booster.gradle

import com.android.build.api.instrumentation.AsmClassVisitorFactory
import com.android.build.api.instrumentation.ClassContext
import com.android.build.api.instrumentation.ClassData
import com.android.build.api.instrumentation.InstrumentationParameters
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFile
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.CompileClasspath
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.ClassWriter

/**
 * The serializable snapshot of a variant which is required to build a
 * [com.didiglobal.booster.transform.TransformContext] inside AGP instrumentation.
 *
 * Only the inputs which really affect the transformation are declared as `@Input`, so that
 * a change of a single class only causes that class (or the jar it belongs to) to be re-instrumented.
 */
interface BoosterInstrumentationParameters : InstrumentationParameters {

    @get:Input
    val projectPath: Property<String>

    @get:Input
    val variantName: Property<String>

    @get:Input
    val applicationId: Property<String>

    @get:Input
    val debuggable: Property<Boolean>

    @get:Input
    val dataBindingEnabled: Property<Boolean>

    /**
     * The project properties prefixed with `booster.`
     */
    @get:Input
    val properties: MapProperty<String, String>

    /**
     * The identifiers of runtime dependencies, e.g. `androidx.core:core:1.12.0`, `project :lib`
     */
    @get:Input
    val dependencies: ListProperty<String>

    @get:Internal
    val projectDir: DirectoryProperty

    @get:CompileClasspath
    val bootClasspath: ListProperty<RegularFile>

    @get:InputFiles
    @get:PathSensitive(PathSensitivity.RELATIVE)
    val mergedManifests: ConfigurableFileCollection

    @get:InputFiles
    @get:PathSensitive(PathSensitivity.RELATIVE)
    val symbolList: ConfigurableFileCollection

    @get:Internal
    val service: Property<BoosterInstrumentationService>

}

/**
 * Bridges the [com.didiglobal.booster.transform.Transformer] SPI into AGP instrumentation,
 * so that classes are transformed one by one and the incremental boundaries of the DEX pipeline are preserved.
 *
 * @author johnsonlee
 */
abstract class BoosterAsmClassVisitorFactory : AsmClassVisitorFactory<BoosterInstrumentationParameters> {

    override fun createClassVisitor(classContext: ClassContext, nextClassVisitor: ClassVisitor): ClassVisitor {
        val params = parameters.get()
        val unit = params.service.get().unitOf(params)
        return BoosterClassVisitor(instrumentationContext.apiVersion.get(), nextClassVisitor) { bytecode ->
            unit.transform(params, classContext, bytecode)
        }
    }

    override fun isInstrumentable(classData: ClassData): Boolean = true

}

/**
 * Buffers the whole class, applies the bytecode-level [transform] on [visitEnd], then replays the result to [next].
 *
 * AGP reads classes with [ClassReader.EXPAND_FRAMES] (or without frames), so the result is replayed
 * with [ClassReader.EXPAND_FRAMES] to keep the downstream visitors seeing what they expect.
 */
internal class BoosterClassVisitor(
    api: Int,
    private val next: ClassVisitor,
    private val transform: (ByteArray) -> ByteArray
) : ClassVisitor(api, ClassWriter(0)) {

    override fun visitEnd() {
        super.visitEnd()
        val bytecode = (cv as ClassWriter).toByteArray()
        ClassReader(transform(bytecode)).accept(next, ClassReader.EXPAND_FRAMES)
    }

}
