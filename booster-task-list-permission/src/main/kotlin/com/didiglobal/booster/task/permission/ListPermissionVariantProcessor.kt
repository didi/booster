package com.didiglobal.booster.task.permission

import com.android.build.api.artifact.SingleArtifact
import com.android.build.api.variant.Variant
import com.android.build.gradle.internal.tasks.factory.dependsOn
import com.didiglobal.booster.BOOSTER
import com.didiglobal.booster.gradle.project
import com.didiglobal.booster.kotlinx.capitalized
import com.didiglobal.booster.task.spi.VariantProcessor
import com.google.auto.service.AutoService
import org.gradle.api.UnknownTaskException

private const val TASK_NAME = "listPermissions"

@AutoService(VariantProcessor::class)
class ListPermissionVariantProcessor : VariantProcessor {

    override fun process(variant: Variant) {
        variant.project.tasks.let { tasks ->
            val listPermissions = try {
                tasks.named(TASK_NAME)
            } catch (e: UnknownTaskException) {
                tasks.register("listPermissions") {
                    it.group = BOOSTER
                    it.description = "List the permissions declared in AndroidManifest.xml"
                }
            }
            tasks.register("list${variant.name.capitalized()}Permissions", ListPermission::class.java) {
                it.group = BOOSTER
                it.description = "List the permission declared in AndroidManifest.xml for ${variant.name}"
                it.outputs.upToDateWhen { false }
                it.mergedManifest.set(variant.artifacts.get(SingleArtifact.MERGED_MANIFEST))
            }.also {
                listPermissions.dependsOn(it)
            }
        }
    }

}
