package com.didiglobal.booster.task.compression.compressor

import com.didiglobal.booster.task.compression.Build
import java.io.File

/**
 * Represents utility class for pngquant operation
 *
 * @author johnsonlee
 */
class Pngquant private constructor(private val path: String = System.getenv("PATH")): ImageCompressor {

    override val executable = path.split(File.pathSeparatorChar).map(::File).map { path ->
        when {
            path.isDirectory -> path.listFiles { file ->
                file.name == PNGQUANT && !file.isDirectory
            }.firstOrNull()
            else -> if (path.name == PNGQUANT) path else null
        }
    }.find {
        it != null && it.exists()
    }

    val isInstalled = executable?.exists() ?: false

    companion object {

        const val EXIT_LARGER_THAN_ORIGINAL = 98

        const val EXIT_UNSATISFIED_QUALITY = 99

        fun find(path: String? = null) = if (null != path) Pngquant(path) else Pngquant()

    }

}

private const val PROGRAM = "pngquant"

private val PNGQUANT = if (System.getProperty("os.name").contains("win", true)) "$PROGRAM.exe" else PROGRAM

internal val PROPERTY_PNGQUANT = Build.ARTIFACT.replace('-', '.') + ".pngquant"
