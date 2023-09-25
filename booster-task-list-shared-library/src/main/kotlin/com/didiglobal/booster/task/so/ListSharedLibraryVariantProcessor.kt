package com.didiglobal.booster.task.so

import com.android.build.api.variant.Variant
import com.android.build.gradle.internal.tasks.factory.dependsOn
import com.didiglobal.booster.BOOSTER
import com.didiglobal.booster.gradle.project
import com.didiglobal.booster.kotlinx.capitalized
import com.didiglobal.booster.task.spi.VariantProcessor
import com.google.auto.service.AutoService
import org.gradle.api.UnknownTaskException

private const val TASK_NAME = "listSharedLibraries"

@AutoService(VariantProcessor::class)
class ListSharedLibraryVariantProcessor : VariantProcessor {

    override fun process(variant: Variant) {
        variant.project.tasks.let { tasks ->
            val listSharedLibraries = try {
                tasks.named(TASK_NAME)
            } catch (e: UnknownTaskException) {
                tasks.register(TASK_NAME) {
                    it.group = BOOSTER
                    it.description = "List the shared libraries that current project depends on"
                }
            }
            tasks.register("list${variant.name.capitalized()}SharedLibraries", ListSharedLibrary::class.java) {
                it.group = BOOSTER
                it.description = "List the shared libraries that current project depends on for ${variant.name}"
                it.outputs.upToDateWhen { false }
                it.variant = variant
            }.also {
                listSharedLibraries.dependsOn(it)
            }
        }
    }

}