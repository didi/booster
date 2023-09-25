package com.didiglobal.booster.task.artifact

import com.android.build.api.variant.Variant
import com.android.build.gradle.internal.tasks.factory.dependsOn
import com.didiglobal.booster.BOOSTER
import com.didiglobal.booster.gradle.project
import com.didiglobal.booster.kotlinx.capitalized
import com.didiglobal.booster.task.spi.VariantProcessor
import com.google.auto.service.AutoService
import org.gradle.api.UnknownTaskException

private const val TASK_NAME = "listArtifacts"

@AutoService(VariantProcessor::class)
class ListArtifactVariantProcessor : VariantProcessor {

    override fun process(variant: Variant) {
        variant.project.tasks.let { tasks ->
            val listArtifacts = try {
                tasks.named(TASK_NAME)
            } catch (e: UnknownTaskException) {
                tasks.register(TASK_NAME) {
                    it.group = BOOSTER
                    it.description = "List build artifacts"
                }
            }
            tasks.register("list${variant.name.capitalized()}Artifacts", ListArtifact::class.java) {
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
