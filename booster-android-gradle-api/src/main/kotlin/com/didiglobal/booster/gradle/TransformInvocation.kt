package com.didiglobal.booster.gradle

import com.android.build.api.transform.TransformInvocation
import com.android.build.gradle.AppExtension
import com.android.build.gradle.BaseExtension
import com.android.build.gradle.LibraryExtension
import com.android.build.gradle.api.BaseVariant
import com.didiglobal.booster.kotlinx.head
import com.didiglobal.booster.util.search
import org.gradle.api.Project
import org.gradle.api.internal.AbstractTask
import org.objectweb.asm.ClassReader
import org.objectweb.asm.tree.ClassNode
import java.io.File

/**
 * Represents the booster transform for
 *
 * @author johnsonlee
 */
val TransformInvocation.project: Project
    get() = (this.context as AbstractTask).project

/**
 * Returns the corresponding variant of this transform invocation
 *
 * @author johnsonlee
 */
val TransformInvocation.variant: BaseVariant
    get() = project.getAndroid<BaseExtension>().let { android ->
        when (android) {
            is AppExtension -> return android.applicationVariants.single { it.name == this.context.variantName }
            is LibraryExtension -> return android.libraryVariants.single { it.name == this.context.variantName }
        }
        TODO("variant not found")
    }

/**
 * Returns the compile classpath of this transform invocation
 *
 * @author johnsonlee
 */
val TransformInvocation.compileClasspath: Collection<File>
    get() = listOf(inputs, referencedInputs).flatten().map {
        it.jarInputs + it.directoryInputs
    }.flatten().map {
        it.file
    }

/**
 * Returns the runtime classpath of this transform invocation
 *
 * @author johnsonlee
 */
val TransformInvocation.runtimeClasspath: Collection<File>
    get() = compileClasspath + project.getAndroid<BaseExtension>().bootClasspath

/**
 * Returns the application id
 */
val TransformInvocation.applicationId: String
    get() {
        val packages = variant.scope.symbolListWithPackageName.filter {
            it.length() > 0
        }.map {
            it.head()!!
        }.toSet()

        return variant.scope.javac.map { classes ->
            val base = classes.toURI()
            classes.search { file ->
                file.name == "BuildConfig.class" && file.inputStream().use { bytecode ->
                    ClassNode().also { klass ->
                        ClassReader(bytecode).accept(klass, 0)
                    }.fields.any {
                        it.name == "APPLICATION_ID" && it.desc == "Ljava/lang/String;" && packages.contains(it.value)
                    }
                }
            }.map {
                base.relativize(it.toURI()).path.substringBeforeLast('/').replace('/', '.')
            }.toSet()
        }.flatten().single()
    }
