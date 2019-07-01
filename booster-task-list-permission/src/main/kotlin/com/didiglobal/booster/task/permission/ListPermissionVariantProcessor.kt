package com.didiglobal.booster.task.permission

import com.android.build.gradle.api.BaseVariant
import com.didiglobal.booster.gradle.scope
import com.didiglobal.booster.task.spi.VariantProcessor
import com.google.auto.service.AutoService

@AutoService(VariantProcessor::class)
class ListPermissionVariantProcessor : VariantProcessor {

    override fun process(variant: BaseVariant) {
        val tasks = variant.scope.globalScope.project.tasks
        val listPermission = tasks.findByName("listPermissions") ?: tasks.create("listPermissions")
        tasks.create("list${variant.name.capitalize()}Permissions", ListPermission::class.java) {
            it.variant = variant
            it.outputs.upToDateWhen { false }
        }.also {
            listPermission.dependsOn(it)
        }
    }

}
