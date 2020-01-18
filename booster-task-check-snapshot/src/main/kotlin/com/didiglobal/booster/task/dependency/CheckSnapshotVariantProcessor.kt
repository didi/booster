package com.didiglobal.booster.task.dependency

import com.android.build.gradle.api.BaseVariant
import com.didiglobal.booster.gradle.javaCompilerTask
import com.didiglobal.booster.gradle.project
import com.didiglobal.booster.task.spi.VariantProcessor
import com.google.auto.service.AutoService

@AutoService(VariantProcessor::class)
class CheckSnapshotVariantProcessor : VariantProcessor {

    override fun process(variant: BaseVariant) {
        val tasks = variant.project.tasks
        val checkSnapshot = tasks.findByName("checkSnapshot") ?: tasks.create("checkSnapshot")
        tasks.create("check${variant.name.capitalize()}Snapshot", CheckSnapshot::class.java) {
            it.variant = variant
            it.outputs.upToDateWhen { false }
        }.also {
            variant.javaCompilerTask.dependsOn(it)
            checkSnapshot.dependsOn(it)
        }
    }

}
