package com.didiglobal.booster.gradle

import com.android.build.gradle.AppExtension
import com.android.build.gradle.LibraryExtension
import com.didiglobal.booster.task.spi.VariantProcessor
import org.gradle.api.Plugin
import org.gradle.api.Project
import java.util.ServiceLoader

/**
 * Represents the booster gradle plugin
 *
 * @author johnsonlee
 */
class BoosterPlugin : Plugin<Project> {

    override fun apply(project: Project) {
        when {
            project.plugins.hasPlugin("com.android.application") || project.plugins.hasPlugin("com.android.dynamic-feature") -> project.getAndroid<AppExtension>().let { android ->
                android.registerTransform(BoosterAppTransform(project))
                project.afterEvaluate {
                    ServiceLoader.load(VariantProcessor::class.java, javaClass.classLoader).toList().let { processors ->
                        android.applicationVariants.forEach { variant ->
                            processors.forEach { processor ->
                                processor.process(variant)
                            }
                        }
                    }
                }
            }
            project.plugins.hasPlugin("com.android.library") -> project.getAndroid<LibraryExtension>().let { android ->
                android.registerTransform(BoosterLibTransform(project))
                project.afterEvaluate {
                    ServiceLoader.load(VariantProcessor::class.java, javaClass.classLoader).toList().let { processors ->
                        android.libraryVariants.forEach { variant ->
                            processors.forEach { processor ->
                                processor.process(variant)
                            }
                        }
                    }
                }
            }
        }
    }

}
