package com.didiglobal.booster.task.profile

import com.didiglobal.booster.build.AndroidSdk
import com.didiglobal.booster.kotlinx.Wildcard
import com.didiglobal.booster.kotlinx.search
import com.github.javaparser.JavaParser
import com.github.javaparser.ParserConfiguration
import com.github.javaparser.symbolsolver.JavaSymbolSolver
import com.github.javaparser.symbolsolver.resolution.typesolvers.JavaParserTypeSolver
import java.io.File
import java.net.URL
import kotlin.test.Test
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class ProfileTaskTest {

    @Test
    fun `check if lint-apis exists`() {
        assertNotNull(DEFAULT_APIS)
        assertTrue(URL(DEFAULT_APIS).openStream().bufferedReader().use {
            it.readLines().map(Wildcard.Companion::valueOf).toSet()
        }.isNotEmpty())
    }

    fun `parser java source`() {
        println(AndroidSdk.getAndroidJar())
        val sources = AndroidSdk.getLocation().resolve("sources").resolve("android-${AndroidSdk.findPlatform()}")
        val parser = JavaParser(ParserConfiguration().apply {
            this.isAttributeComments = false
            this.languageLevel = ParserConfiguration.LanguageLevel.JAVA_8
            this.setSymbolResolver(JavaSymbolSolver(JavaParserTypeSolver(sources)))
        })
        sources.search() {
            it.extension == "java" && !(Regex("^[A-Z]\\w+Test(Case)?$") matches it.nameWithoutExtension)
        }.forEach { java ->
            val relative = java.toRelativeString(sources)
            val className = relative.substringBefore(".java").replace(File.separator, ".")
            try {
                parser.parse(java).ifSuccessful { unit ->
                    unit.types.filter {
                        it.isPublic
                    }.forEach { type ->
                        val classAnnotations = type.annotations.filter {
                            it.name.asString().endsWith("Thread")
                        }.map {
                            it.resolve().qualifiedName
                        }

                        when {
                            classAnnotations.contains("android.annotation.UiThread") -> {
                                println("@UiThread: ${type.fullyQualifiedName.get()}")
                            }
                            classAnnotations.contains("android.annotation.MainThread") -> {
                                println("@MainThread: ${type.fullyQualifiedName.get()}")
                            }
                        }

                        type.methods.forEach { method ->
                            val methodAnnotations = method.annotations.filter {
                                it.name.asString().endsWith("Thread")
                            }.map {
                                it.resolve().qualifiedName
                            }

                            when {
                                methodAnnotations.contains("android.annotation.UiThread") -> {
                                    println("@UiThread: ${type.fullyQualifiedName.get()}.${method.signature.asString()}")
                                }
                                methodAnnotations.contains("android.annotation.MainThread") -> {
                                    println("@MainThread: ${type.fullyQualifiedName.get()}.${method.signature.asString()}")
                                }
                            }
                        }
                    }
                }
            } catch (e: Throwable) {
                println("Error: ${e.localizedMessage}: ${java}")
            }
        }

    }

}