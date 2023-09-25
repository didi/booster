@file:JvmName("RelativeResourceUtils")

package com.didiglobal.booster

import java.io.File
import java.io.IOException
import java.nio.file.FileSystem
import java.nio.file.FileSystems
import kotlin.IllegalStateException

private const val separator: String = ":/"

/**
 * Determines a resource file path relative to the source set containing the resource.
 *
 * The absolute path to the module source set is identified by the source set ordering of a module.
 * Format of the returned String is `<package name - source set module order>:<path to source set>`.
 */
fun getRelativeSourceSetPath(resourceFile: File, moduleSourceSets: Map<String, String>)
        : String {
    val absoluteResFilePath = resourceFile.absolutePath
    for ((identifier, absoluteSourceSetPath) in moduleSourceSets.entries) {
        if (absoluteResFilePath.startsWith(absoluteSourceSetPath)) {
            val invariantFilePath = resourceFile.absoluteFile.invariantSeparatorsPath
            val resIndex = File(absoluteSourceSetPath).absoluteFile.invariantSeparatorsPath.length
            val relativePathToSourceSet = invariantFilePath.substring(resIndex + 1)
            return "$identifier$separator$relativePathToSourceSet"
        }
    }

    throw IllegalArgumentException(
        "Unable to locate resourceFile ($absoluteResFilePath) in source-sets.")
}

/**
 * Converts a source set identified relative resource path to an absolute path.
 *
 * The source set identifier before the separator is replaced with the absolute source set
 * path and then concatenated with the path after the separator.
 */
fun relativeResourcePathToAbsolutePath(
    relativePath: String,
    sourceSetPathMap: Map<String, String>,
    fileSystem: FileSystem = FileSystems.getDefault()): String {
    return relativeResourcePathToAbsolutePath(sourceSetPathMap, fileSystem)(relativePath)
}

fun relativeResourcePathToAbsolutePath(
    sourceSetPathMap: Map<String, String>,
    fileSystem: FileSystem = FileSystems.getDefault()
): (String) -> String {
    return { relativePath: String ->
        if (sourceSetPathMap.none()) {
            throw IllegalStateException(
                """Unable to get absolute path from $relativePath
                   because no relative root paths are present."""
            )
        }
        val separatorIndex = relativePath.indexOf(separator)
        if (separatorIndex == -1) {
            throw IllegalArgumentException(
                """Source set identifier and relative path must be separated by a "$separator".
                   Relative path: $relativePath"""
            )
        }
        val sourceSetPrefix = relativePath.substring(0, separatorIndex)
        val resourcePathFromSourceSet =
            relativePath.substring(separatorIndex + separator.lastIndex, relativePath.length)
        val systemRelativePath = if ("/" != fileSystem.separator) {
            resourcePathFromSourceSet.replace("/", fileSystem.separator)
        } else {
            resourcePathFromSourceSet
        }
        val absolutePath = sourceSetPathMap[sourceSetPrefix]
            ?: throw NoSuchElementException(
                """Unable to get absolute path from $relativePath
                       because $sourceSetPrefix is not key in sourceSetPathMap."""
            )
        "$absolutePath$systemRelativePath"
    }
}

/**
 * Parses identifier and file path into a map from a file
 * in the format 'packageName.projectName-sortedOrderPosition absolutePath'.
 */
fun readFromSourceSetPathsFile(artifactFile: File): Map<String, String> {
    if (!artifactFile.exists() || !artifactFile.isFile) {
        throw IOException("$artifactFile does not exist or is not a file.")
    }
    return artifactFile.bufferedReader().use { bufferedReader ->
        bufferedReader.lineSequence().associate {
            it.substringBefore(" ") to it.substringAfter(" ")
        }
    }
}

/**
 * Writes a file containing a mapping of resource source-set absolute paths to a unique identifier
 * in the format of 'packageName.projectName-sortedOrderPosition absolutePath'.
 */
fun writeIdentifiedSourceSetsFile(
    resourceSourceSets: List<File>,
    namespace: String,
    projectPath: String,
    output: File
) {
    output.bufferedWriter().use { bw ->
        getIdentifiedSourceSetMap(resourceSourceSets, namespace, projectPath).forEach {
            bw.write("${it.key} ${it.value}\n")
        }
    }
}

/**
 * Using a list of files following the format produced by writeIdentifiedSourceSetsFile,
 * contents of each file are added to a single table which maps the source set identifier
 * to the absolute path of the source set.
 */
fun mergeIdentifiedSourceSetFiles(sourceSetFiles: Collection<File>) : Map<String, String> {
    return mutableMapOf<String,String>()
        .also { identifiedSourceMap ->
            sourceSetFiles
                .map { readFromSourceSetPathsFile(it) }
                .forEach { identifiedSourceMap.putAll(it) }
        }
}

fun getIdentifiedSourceSetMap(
    resourceSourceSets: List<File>,
    namespace: String,
    projectPath: String) : Map<String, String> {
    val projectName = projectPath.substringAfterLast(":")
    var i = 0
    return resourceSourceSets
        .asSequence()
        .filterNotNull()
        .distinctBy(File::invariantSeparatorsPath)
        .sortedBy(File::invariantSeparatorsPath)
        .associate { sourceSet ->
            val sourceSetFolderName = sourceSet.parentFile.name
            val appendProjectName =
                if (namespace.endsWith(projectName)) "" else ".$projectName"
            val appId = "$namespace$appendProjectName-$sourceSetFolderName-${i++}"
            appId to sourceSet.absolutePath
        }
}

/**
 * Verifies if a string is relative resource sourceset filepath. This is for cases where it is
 * not possible to determine if relative resource filepaths are enabled by default.
 */
fun isRelativeSourceSetResource(filepath: String) : Boolean {
    return filepath.contains(separator)
}
