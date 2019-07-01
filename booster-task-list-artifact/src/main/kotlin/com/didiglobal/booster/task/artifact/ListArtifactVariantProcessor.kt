package com.didiglobal.booster.task.artifact

import com.android.build.gradle.api.BaseVariant
import com.didiglobal.booster.gradle.scope
import com.didiglobal.booster.task.spi.VariantProcessor
import com.google.auto.service.AutoService

@AutoService(VariantProcessor::class)
class ListArtifactVariantProcessor : VariantProcessor {

    override fun process(variant: BaseVariant) {
        val tasks = variant.scope.globalScope.project.tasks
        val artifacts = tasks.findByName("listArtifacts") ?: tasks.create("listArtifacts")
        tasks.create("list${variant.name.capitalize()}Artifacts", ListArtifact::class.java) {
            it.variant = variant
            it.outputs.upToDateWhen { false }
        }.also {
            artifacts.dependsOn(it)
        }
    }

}
