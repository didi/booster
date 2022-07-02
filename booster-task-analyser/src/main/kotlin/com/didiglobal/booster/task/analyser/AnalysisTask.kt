package com.didiglobal.booster.task.analyser

import com.android.build.gradle.api.BaseVariant
import com.didiglobal.booster.BOOSTER
import com.didiglobal.booster.cha.asm.AsmClassSetCache
import com.didiglobal.booster.kotlinx.touch
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Internal
import java.io.File

abstract class AnalysisTask : DefaultTask() {

    @get:Internal
    var variant: BaseVariant? = null

    @get:Internal
    lateinit var classSetCache: AsmClassSetCache

    @Internal
    final override fun getGroup(): String = BOOSTER

    @Internal
    abstract override fun getDescription(): String

    abstract fun analyse()

}

internal val AnalysisTask.reportDir: File
    get() = project.buildDir
            .resolve("reports")
            .resolve(Build.ARTIFACT)
            .resolve(javaClass.kotlin.category)
            .resolve(variant?.dirName ?: ".")

internal fun AnalysisTask.report(name: String): File {
    return reportDir.resolve(name).resolve("index.${name}").touch()
}
