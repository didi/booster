package com.didiglobal.booster.gradle

import com.didiglobal.booster.transform.Transformer
import org.gradle.api.Project
import org.gradle.api.initialization.dsl.ScriptHandler
import org.gradle.api.plugins.PluginContainer
import java.io.Serializable

data class TransformParameter(
        val name: String,
        val buildscript: ScriptHandler,
        val plugins: PluginContainer,
        val properties: Map<String, Any?>,
        val transformers: Set<Class<Transformer>>
) : Serializable

fun Project.newTransformParameter(name: String): TransformParameter {
    return TransformParameter(name, buildscript, plugins, properties, lookupTransformers(buildscript.classLoader))
}