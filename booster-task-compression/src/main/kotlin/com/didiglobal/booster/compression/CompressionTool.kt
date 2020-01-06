package com.didiglobal.booster.compression

import java.io.File

/**
 * Represents a compression tool
 *
 * @author johnsonlee
 *
 * @param name The executable name
 * @param path The search path
 * @param options The compression options
 */
abstract class CompressionTool(val name: String, val path: String) : CompressionTaskCreatorFactory {

    val executable: File?
        get() = (path.takeIf { it.isNotBlank() && File(it, name).exists() } ?: System.getenv("PATH")).split(File.pathSeparatorChar).map {
            File(it)
        }.map { path ->
            when {
                path.isDirectory -> path.listFiles { file ->
                    file.name == name && !file.isDirectory
                }?.firstOrNull()
                else -> if (path.name == name) path else null
            }
        }.find {
            it != null && it.exists()
        }

    val isInstalled = executable?.exists() ?: false

    open fun install(location: File): Boolean = isInstalled


}
