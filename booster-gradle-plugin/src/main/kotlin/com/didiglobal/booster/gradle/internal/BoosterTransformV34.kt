package com.didiglobal.booster.gradle.internal

import com.android.build.api.variant.VariantInfo
import com.didiglobal.booster.gradle.BoosterTransform
import org.gradle.api.Project

internal class BoosterTransformV34(project: Project) : BoosterTransform(project) {

    @Suppress("UnstableApiUsage")
    override fun applyToVariant(variant: VariantInfo): Boolean {
        return variant.buildTypeEnabled || (variant.flavorNames.isNotEmpty() && variant.fullVariantEnabled)
    }

    @Suppress("UnstableApiUsage")
    private val VariantInfo.fullVariantEnabled: Boolean
        get() = project.findProperty("booster.transform.${fullVariantName}.enabled")?.toString()?.toBoolean() ?: true

    @Suppress("UnstableApiUsage")
    private val VariantInfo.buildTypeEnabled: Boolean
        get() = project.findProperty("booster.transform.${buildTypeName}.enabled")?.toString()?.toBoolean() ?: true

}

