package com.didiglobal.booster.gradle

import com.android.build.gradle.AppExtension
import com.android.build.gradle.LibraryExtension
import com.didiglobal.booster.annotations.Priority
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
        project.gradle.addListener(BoosterTransformTaskExecutionListener(project))

        when {
            project.plugins.hasPlugin("com.android.application") || project.plugins.hasPlugin("com.android.dynamic-feature") -> project.getAndroid<AppExtension>().let { android ->
                android.registerTransform(BoosterTransform(project))
                project.afterEvaluate {
                    ServiceLoader.load(VariantProcessor::class.java, project.buildscript.classLoader).sortedBy {
                        it.javaClass.getAnnotation(Priority::class.java)?.value ?: 0
                    }.let { processors ->
                        android.applicationVariants.forEach { variant ->
                            processors.forEach { processor ->
                                processor.process(variant)
                            }
                        }
                    }
                }
            }
            project.plugins.hasPlugin("com.android.library") -> project.getAndroid<LibraryExtension>().let { android ->
                android.registerTransform(BoosterTransform(project))
                project.afterEvaluate {
                    ServiceLoader.load(VariantProcessor::class.java, project.buildscript.classLoader).sortedBy {
                        it.javaClass.getAnnotation(Priority::class.java)?.value ?: 0
                    }.let { processors ->
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
