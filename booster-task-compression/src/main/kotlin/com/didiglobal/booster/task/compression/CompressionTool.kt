package com.didiglobal.booster.task.compression

import com.android.SdkConstants
import com.android.build.gradle.BaseExtension
import com.didiglobal.booster.gradle.getAndroid
import com.didiglobal.booster.kotlinx.file
import com.didiglobal.booster.task.compression.cwebp.Cwebp
import com.didiglobal.booster.task.compression.pngquant.Pngquant
import org.gradle.api.Project
import java.io.File

/**
 * Represents a compression tool
 *
 * @author johnsonlee
 */
abstract class CompressionTool(val name: String, val path: String? = null) : CompressionTaskCreatorFactory {

    val executable: File?
        get() = (path ?: System.getenv("PATH")).split(File.pathSeparatorChar).map {
            File(it)
        }.map { path ->
            when {
                path.isDirectory -> path.listFiles { file ->
                    file.name == name && !file.isDirectory
                }.firstOrNull()
                else -> if (path.name == name) path else null
            }
        }.find {
            it != null && it.exists()
        }

    val isInstalled = executable?.exists() ?: false

    open fun install(location: File): Boolean = isInstalled

    companion object {

        /**
         * Select the best compressor
         */
        fun get(project: Project): CompressionTool? {
            val pngquant = project.findProperty(PROPERTY_PNGQUANT)?.toString()
            val compressor = project.findProperty(PROPERTY_COMPRESSOR)?.toString()
            val binDir = project.buildDir.file(SdkConstants.FD_OUTPUT).absolutePath
            val minSdkVersion = project.getAndroid<BaseExtension>().defaultConfig.minSdkVersion.apiLevel

            project.logger.info("minSdkVersion: $minSdkVersion$")
            project.logger.info("$PROPERTY_COMPRESSOR: $compressor")
            project.logger.info("$PROPERTY_PNGQUANT: $pngquant")

            return when (compressor) {
                Cwebp.PROGRAM -> Cwebp(binDir)
                Pngquant.PROGRAM -> Pngquant(pngquant)
                else -> when {
                    minSdkVersion >= 18 -> Cwebp(binDir)
                    minSdkVersion in 15..17 -> Cwebp(binDir, true)
                    else -> Pngquant(pngquant).let {
                        if (it.isInstalled) it else null
                    }
                }
            }
        }

    }

}
