package com.didiglobal.booster.task.compression

import com.android.build.gradle.api.BaseVariant
import org.gradle.api.Task
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
    fun getCompressionTaskClass(aapt2: Boolean): KClass<out CompressImages>

    /**
     * Returns a task for assets compression
     */
    fun createAssetsCompressionTask(variant: BaseVariant, results: CompressionResults): Task

    /**
     * Returns a task for resource compression
     */
    fun createResourcesCompressionTask(variant: BaseVariant, results: CompressionResults): Task

}
