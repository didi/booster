package com.didiglobal.booster.task.permission

import com.android.build.gradle.api.BaseVariant
import com.android.build.gradle.internal.tasks.factory.dependsOn
import com.didiglobal.booster.BOOSTER
import com.didiglobal.booster.gradle.project
import com.didiglobal.booster.task.spi.VariantProcessor
import com.google.auto.service.AutoService
import org.gradle.api.UnknownTaskException

private const val TASK_NAME = "listPermissions"

@AutoService(VariantProcessor::class)
class ListPermissionVariantProcessor : VariantProcessor {

    override fun process(variant: BaseVariant) {
        variant.project.tasks.let { tasks ->
            val listPermissions = try {
                tasks.named(TASK_NAME)
            } catch (e: UnknownTaskException) {
                tasks.register("listPermissions") {
                    it.group = BOOSTER
                    it.description = "List the permissions declared in AndroidManifest.xml"
                }
            }
            @Suppress("DEPRECATION")
            tasks.register("list${variant.name.capitalize()}Permissions", ListPermission::class.java) {
                it.group = BOOSTER
                it.description = "List the permission declared in AndroidManifest.xml for ${variant.name}"
                it.variant = variant
                it.outputs.upToDateWhen { false }
            }.also {
                listPermissions.dependsOn(it)
            }
        }
    }

}
