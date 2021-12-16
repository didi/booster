package com.didiglobal.booster.transform.util

import com.didiglobal.booster.kotlinx.search
import com.didiglobal.booster.transform.TransformContext
import org.apache.commons.compress.archivers.ArchiveEntry
import org.apache.commons.compress.archivers.ArchiveStreamFactory
import java.io.IOException

interface Collector<R> {
    fun accept(name: String): Boolean
    fun collect(name: String, data: () -> ByteArray): R
}

sealed class Collectors {

    object ClassNameCollector : Collector<String> {
        override fun accept(name: String): Boolean {
            return name.endsWith(".class", true)
        }

        override fun collect(name: String, data: () -> ByteArray): String {
            return name.substringBeforeLast('.').replace('/', '.')
        }
    }

    object ServiceCollector : Collector<Pair<String, Collection<String>>> {
        override fun accept(name: String): Boolean {
            return name.substringBeforeLast('/') == "META-INF/services"
        }

        override fun collect(name: String, data: () -> ByteArray): Pair<String, Collection<String>> {
            return name.substringAfterLast('/').replace('/', '.') to data().inputStream().bufferedReader().lineSequence().filterNot {
                it.isBlank() || it.startsWith('#')
            }.toMutableSet()
        }
    }

}

fun <R> TransformContext.collect(collector: Collector<R>): List<R> = compileClasspath.map { file ->
    when {
        file.isDirectory -> {
            val base = file.toURI()
            file.search { f ->
                f.isFile && collector.accept(base.relativize(f.toURI()).normalize().path)
            }.map { f ->
                collector.collect(base.relativize(f.toURI()).normalize().path, f::readBytes)
            }
        }
        file.isFile -> {
            file.inputStream().buffered().use {
                ArchiveStreamFactory().createArchiveInputStream(it).let { archive ->
                    generateSequence {
                        try {
                            archive.nextEntry
                        } catch (e: IOException) {
                            null
                        }
                    }.filterNot(ArchiveEntry::isDirectory).filter { entry ->
                        collector.accept(entry.name)
                    }.map { entry ->
                        collector.collect(entry.name, archive::readBytes)
                    }.toList()
                }
            }
        }
        else -> emptyList()
    }
}.flatten()