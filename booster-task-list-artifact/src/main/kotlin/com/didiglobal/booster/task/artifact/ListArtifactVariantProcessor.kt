package com.didiglobal.booster.task.artifact

import com.android.build.gradle.api.BaseVariant
import com.android.build.gradle.internal.tasks.factory.dependsOn
import com.didiglobal.booster.BOOSTER
import com.didiglobal.booster.gradle.project
import com.didiglobal.booster.task.spi.VariantProcessor
import com.google.auto.service.AutoService
import org.gradle.api.UnknownTaskException

private const val TASK_NAME = "listArtifacts"

@AutoService(VariantProcessor::class)
class ListArtifactVariantProcessor : VariantProcessor {

    override fun process(variant: BaseVariant) {
        variant.project.tasks.let { tasks ->
            val listArtifacts = try {
                tasks.named(TASK_NAME)
            } catch (e: UnknownTaskException) {
                tasks.register(TASK_NAME) {
                    it.group = BOOSTER
                    it.description = "List build artifacts"
                }
            }
            @Suppress("DEPRECATION")
            tasks.register("list${variant.name.capitalize()}Artifacts", ListArtifact::class.java) {
                it.group = BOOSTER
                it.description = "List build artifacts for ${variant.name}"
                it.variant = variant
                it.outputs.upToDateWhen { false }
            }.also {
                listArtifacts.dependsOn(it)
            }
        }
    }

}
