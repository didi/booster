package com.didiglobal.booster.task.dependency

import com.android.build.gradle.api.BaseVariant
import com.android.build.gradle.internal.tasks.factory.dependsOn
import com.didiglobal.booster.BOOSTER
import com.didiglobal.booster.gradle.javaCompilerTaskProvider
import com.didiglobal.booster.gradle.project
import com.didiglobal.booster.task.spi.VariantProcessor
import com.google.auto.service.AutoService
import org.gradle.api.UnknownTaskException

private const val TASK_NAME = "checkSnapshot"

@AutoService(VariantProcessor::class)
class CheckSnapshotVariantProcessor : VariantProcessor {

    override fun process(variant: BaseVariant) {
        variant.project.tasks.let { tasks ->
            val checkSnapshot = try {
                tasks.named(TASK_NAME)
            } catch (e: UnknownTaskException) {
                tasks.register(TASK_NAME) {
                    it.group = BOOSTER
                    it.description = "Check snapshot dependencies"
                }
            }
            @Suppress("DEPRECATION")
            tasks.register("check${variant.name.capitalize()}Snapshot", CheckSnapshot::class.java) {
                it.group = BOOSTER
                it.description = "Check snapshot dependencies for ${variant.name}"
                it.variant = variant
                it.outputs.upToDateWhen { false }
            }.also {
                variant.javaCompilerTaskProvider.dependsOn(it)
                checkSnapshot.dependsOn(it)
            }
        }
    }

}
