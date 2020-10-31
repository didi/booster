package com.didiglobal.booster.gradle

import com.android.build.gradle.api.BaseVariant
import com.android.build.gradle.internal.publishing.AndroidArtifacts.ArtifactScope.ALL
import com.android.build.gradle.internal.publishing.AndroidArtifacts.ArtifactType.AAR
import com.android.build.gradle.internal.publishing.AndroidArtifacts.ArtifactType.JAR
import com.android.build.gradle.internal.publishing.AndroidArtifacts.ConsumedConfigType.RUNTIME_CLASSPATH
import com.didiglobal.booster.kotlinx.file
import com.didiglobal.booster.kotlinx.separatorsToSystem
import com.didiglobal.booster.kotlinx.touch
import org.gradle.api.artifacts.component.ProjectComponentIdentifier
import org.gradle.api.artifacts.result.ResolvedArtifactResult
import java.io.File
import java.io.PrintWriter

/**
 * Represents the dependencies of the specified variant
 *
 * @author johnsonlee
 */
@Suppress("UnstableApiUsage")
class ResolvedArtifactResults(private val variant: BaseVariant) : Collection<ResolvedArtifactResult> {

    private val results = listOf(AAR, JAR)
            .asSequence()
            .map { variant.getArtifactCollection(RUNTIME_CLASSPATH, ALL, it) }
            .map { it.artifacts }
            .flatten()
            .filter { it.id.componentIdentifier !is ProjectComponentIdentifier }
            .distinctBy { it.id.componentIdentifier.displayName }
            .sortedBy { it.id.componentIdentifier.displayName }
            .toList()

    private val maxNameWidth = map { it.id.componentIdentifier.displayName.length }.max() ?: 0

    private val maxFileWidth = map { it.file.path.length }.max() ?: 0

    override val size: Int
        get() = results.count()

    override fun contains(element: ResolvedArtifactResult) = results.contains(element)

    override fun containsAll(elements: Collection<ResolvedArtifactResult>) = results.containsAll(elements)

    override fun isEmpty() = results.iterator().hasNext()

    override fun iterator() = results.iterator()

    /**
     * Default dependency stringify
     */
    private val stringify: (ResolvedArtifactResult) -> String = { result ->
        result.id.componentIdentifier.displayName + " ".repeat(maxNameWidth + 1 - result.id.componentIdentifier.displayName.length) + result.file + " ".repeat(maxFileWidth + 1 - result.file.path.length) + result.file.length()
    }

    /**
     * Dump it to file with specific stringify
     */
    fun dump(file: File = makeDependenciesOutput(), stringifier: (ResolvedArtifactResult) -> String = this.stringify) {
        file.touch().printWriter().use {
            print(it, stringifier)
        }
    }

    /**
     * Print all component artifacts
     */
    fun print(printer: PrintWriter = PrintWriter(System.out, true), stringify: (ResolvedArtifactResult) -> String = this.stringify) {
        forEach { result ->
            printer.apply {
                println(stringify(result))
            }.flush()
        }
    }

    /**
     * Default output location: $buildDir/intermediates/dependencies/${variantDirName}/dependencies.txt
     */
    private fun makeDependenciesOutput() = AGP.run { variant.globalScope }.intermediatesDir.file(
            "dependencies",
            variant.dirName.separatorsToSystem(),
            "dependencies.txt"
    )

}
