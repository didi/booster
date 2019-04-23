package com.didiglobal.booster.task.permission

import com.android.build.gradle.api.BaseVariant
import com.didiglobal.booster.gradle.scope
import com.didiglobal.booster.task.spi.VariantProcessor
import com.google.auto.service.AutoService

@AutoService(VariantProcessor::class)
class PermissionVariantProcessor : VariantProcessor {

    override fun process(variant: BaseVariant) {
        val tasks = variant.scope.globalScope.project.tasks
        val showPermission = tasks.findByName("showPermissions") ?: tasks.create("showPermissions")
        tasks.create("show${variant.name.capitalize()}Permissions", PermissionExtractor::class.java) {
            it.variant = variant
            it.outputs.upToDateWhen { false }
        }.also {
            showPermission.dependsOn(it)
        }
    }

}
