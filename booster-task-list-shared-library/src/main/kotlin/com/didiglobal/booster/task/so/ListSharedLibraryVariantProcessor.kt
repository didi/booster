package com.didiglobal.booster.task.so

import com.android.build.gradle.api.BaseVariant
import com.didiglobal.booster.gradle.scope
import com.didiglobal.booster.task.spi.VariantProcessor
import com.google.auto.service.AutoService

@AutoService(VariantProcessor::class)
class ListSharedLibraryVariantProcessor : VariantProcessor {

    override fun process(variant: BaseVariant) {
        variant.scope.globalScope.project.tasks.let { tasks ->
            val listSharedLibraries = tasks.findByName("listSharedLibraries") ?: tasks.create("listSharedLibraries")
            tasks.create("list${variant.name.capitalize()}SharedLibraries", ListSharedLibrary::class.java) {
                it.outputs.upToDateWhen { false }
                it.variant = variant
            }.also {
                listSharedLibraries.dependsOn(it)
            }
        }
    }

}