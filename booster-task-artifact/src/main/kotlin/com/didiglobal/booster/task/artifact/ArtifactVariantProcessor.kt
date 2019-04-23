package com.didiglobal.booster.task.artifact

import com.android.build.gradle.api.BaseVariant
import com.didiglobal.booster.gradle.scope
import com.didiglobal.booster.task.spi.VariantProcessor
import com.google.auto.service.AutoService

@AutoService(VariantProcessor::class)
class ArtifactVariantProcessor : VariantProcessor {

    override fun process(variant: BaseVariant) {
        val tasks = variant.scope.globalScope.project.tasks
        val artifacts = tasks.findByName("showArtifacts") ?: tasks.create("showArtifacts")
        tasks.create("show${variant.name.capitalize()}Artifacts", ArtifactsResolver::class.java) {
            it.variant = variant
            it.outputs.upToDateWhen { false }
        }.also {
            artifacts.dependsOn(it)
        }
    }

}
