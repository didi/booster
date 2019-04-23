package com.didiglobal.booster.task.dependency

import com.android.build.gradle.api.BaseVariant
import com.didiglobal.booster.gradle.scope
import com.didiglobal.booster.task.spi.VariantProcessor
import com.google.auto.service.AutoService

@AutoService(VariantProcessor::class)
class DependencyVariantProcessor : VariantProcessor {

    override fun process(variant: BaseVariant) {
        val tasks = variant.scope.globalScope.project.tasks
        val checkSnapshot = tasks.findByName("checkSnapshot") ?: tasks.create("checkSnapshot")
        tasks.create("check${variant.name.capitalize()}Snapshot", CheckSnapshot::class.java) {
            it.variant = variant
            it.outputs.upToDateWhen { false }
        }.also {
            variant.javaCompiler.dependsOn(it)
            checkSnapshot.dependsOn(it)
        }
    }

}
