package com.didiglobal.booster.compression

import com.android.build.gradle.api.BaseVariant
import com.didiglobal.booster.compression.task.CompressImages
import com.didiglobal.booster.kotlinx.Wildcard
import org.gradle.api.Task
import java.io.File
import kotlin.reflect.KClass

/**
 * Represents the creator of [CompressImages]
 *
 * @author johnsonlee
 */
interface CompressionTaskCreator {

    /**
     * Returns the class of compression task
     *
     * @param aapt2 A value to determine if aapt2 enabled
     */
    fun getCompressionTaskClass(aapt2: Boolean): KClass<out CompressImages<out CompressionOptions>>

    /**
     * Returns a task for compression
     *
     * @param variant The build variant
     * @param results The compression results for report generating
     * @param name The name of task
     * @param supplier The image supplier
     * @param deps The dependent tasks
     */
    fun createCompressionTask(variant: BaseVariant, results: CompressionResults, name: String, supplier: () -> Collection<File>, vararg deps: Task): CompressImages<out CompressionOptions> = createCompressionTask(variant, results, name, supplier, emptySet(), *deps)

    /**
     * Returns a task for compression
     *
     * @param variant The build variant
     * @param results The compression results for report generating
     * @param name The name of task
     * @param supplier The image supplier
     * @param ignores wildcard of the resource name which to be excluded
     * @param deps The dependent tasks
     */
    fun createCompressionTask(variant: BaseVariant, results: CompressionResults, name: String, supplier: () -> Collection<File>, ignores: Set<Wildcard> = emptySet(), vararg deps: Task): CompressImages<out CompressionOptions>

}
