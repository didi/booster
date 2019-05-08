package com.didiglobal.booster.gradle

import com.android.build.gradle.api.BaseVariant
import com.android.build.gradle.internal.publishing.AndroidArtifacts
import com.didiglobal.booster.kotlinx.file
import com.didiglobal.booster.kotlinx.int
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

    private val results: Iterable<ResolvedArtifactResult>
    private val maxNameWidth: Int
    private val maxFileWidth: Int

    init {
        results = listOf(AndroidArtifacts.ArtifactType.AAR, AndroidArtifacts.ArtifactType.JAR)
                .asSequence()
                .map { variant.variantData.scope.getArtifactCollection(AndroidArtifacts.ConsumedConfigType.RUNTIME_CLASSPATH, AndroidArtifacts.ArtifactScope.ALL, it) }
                .map { it.artifacts }
                .flatten()
                .filter { it.id.componentIdentifier !is ProjectComponentIdentifier }
                .toList()
                .distinctBy { it.id.componentIdentifier.displayName }
                .sortedBy { it.id.componentIdentifier.displayName }
                .toList()
        maxNameWidth = int(map { it.id.componentIdentifier.displayName.length }.max()).value
        maxFileWidth = int(map { it.file.path.length }.max()).value
    }

    override val size = results.count()

    override fun contains(element: ResolvedArtifactResult) = results.contains(element)

    override fun containsAll(elements: Collection<ResolvedArtifactResult>) = results.intersect(elements).size == elements.size

    override fun isEmpty() = results.iterator().hasNext()

    override fun iterator(): Iterator<ResolvedArtifactResult> = results.iterator()

    /**
     * Default output location: $buildDir/intermediates/dependencies/${variantDirName}/dependencies.txt
     */
    private val output = variant.variantData.scope.globalScope.intermediatesDir.file("dependencies").file(variant.dirName.separatorsToSystem()).file("dependencies.txt")

    /**
     * Default dependency stringify
     */
    private val stringify: (ResolvedArtifactResult) -> String = { result ->
        result.id.componentIdentifier.displayName + " ".repeat(maxNameWidth + 1 - result.id.componentIdentifier.displayName.length) + result.file + " ".repeat(maxFileWidth + 1 - result.file.path.length) + result.file.length()
    }

    /**
     * Dump it to file with specific stringify
     */
    fun dump(file: File = output, stringifier: (ResolvedArtifactResult) -> String = this.stringify) {
        file.touch().printWriter().use {
            print(it, stringifier)
        }
    }

    /**
     * Print all component artifacts
     */
    fun print(printer: PrintWriter = PrintWriter(System.out, true), stringifier: (ResolvedArtifactResult) -> String = this.stringify) {
        forEach { result ->
            printer.apply {
                println(stringifier(result))
            }.flush()
        }
    }

}
