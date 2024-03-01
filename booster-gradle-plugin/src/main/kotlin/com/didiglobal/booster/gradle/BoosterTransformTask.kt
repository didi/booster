package com.didiglobal.booster.gradle

import com.android.build.api.variant.Variant
import com.didiglobal.booster.kotlinx.NCPU
import com.didiglobal.booster.kotlinx.search
import com.didiglobal.booster.kotlinx.touch
import com.didiglobal.booster.transform.AbstractTransformContext
import com.didiglobal.booster.transform.Transformer
import com.didiglobal.booster.transform.artifactManager
import com.didiglobal.booster.transform.util.CompositeCollector
import com.didiglobal.booster.transform.util.collect
import com.google.common.collect.Sets
import org.apache.commons.compress.archivers.jar.JarArchiveEntry
import org.apache.commons.compress.archivers.jar.JarArchiveOutputStream
import org.apache.commons.compress.archivers.zip.ParallelScatterZipCreator
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry
import org.apache.commons.compress.parallel.InputStreamSupplier
import org.gradle.api.DefaultTask
import org.gradle.api.file.Directory
import org.gradle.api.file.RegularFile
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import java.io.File
import java.io.IOException
import java.util.concurrent.Executors
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream

abstract class BoosterTransformTask : DefaultTask() {

    @get:Input
    abstract var applicationId: String

    @get:Internal
    abstract var transformers: Collection<Transformer>

    @get:Internal
    abstract var variant: Variant

    @get:Internal
    abstract var bootClasspath: Provider<List<RegularFile>>

    @get:InputFiles
    abstract val allJars: ListProperty<RegularFile>

    @get:InputFiles
    abstract val allDirectories: ListProperty<Directory>

    @get:OutputFile
    abstract val output: RegularFileProperty

    private val entries = Sets.newConcurrentHashSet<String>()

    @TaskAction
    fun taskAction() {
        val context = object : AbstractTransformContext(
            applicationId,
            variant.name,
            bootClasspath.get().map(RegularFile::getAsFile),
            compileClasspath,
            compileClasspath
        ) {
            override val projectDir = project.projectDir
            override val artifacts = variant.artifactManager
        }
        val executor = Executors.newFixedThreadPool(NCPU)
        try {
            transformers.map {
                executor.submit {
                    it.onPreTransform(context)
                }
            }.forEach {
                it.get()
            }

            compileClasspath.map { input ->
                executor.submit {
                    input.takeIf {
                        it.collect(CompositeCollector(context.collectors)).isNotEmpty()
                    }
                }
            }.forEach {
                it.get()
            }
            JarArchiveOutputStream(output.get().asFile.touch().outputStream().buffered()).use { jos ->
                val creator = ParallelScatterZipCreator(
                    ThreadPoolExecutor(
                        NCPU,
                        NCPU,
                        0L,
                        TimeUnit.MILLISECONDS,
                        LinkedBlockingQueue<Runnable>(),
                        Executors.defaultThreadFactory()
                    ) { runnable, _ ->
                        runnable.run()
                    }
                )

                compileClasspath.map {
                    executor.submit {
                        it.transform("", creator) { bytecode ->
                            transformers.fold(bytecode) { bytes, transformer ->
                                transformer.transform(context, bytes)
                            }
                        }
                    }
                }.forEach {
                    it.get()
                }
                creator.writeTo(jos)
            }

            transformers.map {
                executor.submit {
                    it.onPostTransform(context)
                }
            }.forEach {
                it.get()
            }
        } finally {
            executor.shutdown()
            executor.awaitTermination(1L, TimeUnit.HOURS)
        }
    }

    private val compileClasspath: Collection<File>
        get() = (allJars.get() + allDirectories.get()).map {
            it.asFile
        }


    /**
     * Transform this file or directory to the output by the specified transformer
     *
     * @param transformer The byte data transformer
     */
    fun File.transform(
        prefix: String,
        creator: ParallelScatterZipCreator,
        transformer: (ByteArray) -> ByteArray = { it -> it }
    ) {
        when {
            isDirectory -> {
                this.toURI().let { base ->
                    this.search {
                        it.extension.lowercase() == "class"
                    }.parallelStream().forEach {
                        it.transform(creator, base.relativize(it.toURI()).path, transformer)
                    }
                }
            }

            isFile      -> when (extension.lowercase()) {
                "jar" -> ZipInputStream(this.inputStream()).use {
                    it.transform(creator, ::JarArchiveEntry, transformer)
                }

                "class" -> transform(creator, "$prefix/$name".substring(1), transformer)
                else -> println("Not transform file $path")
            }

            else        -> throw IOException("Unexpected file: ${this.canonicalPath}")
        }
    }

    fun File.transform(creator: ParallelScatterZipCreator, name: String, transformer: (ByteArray) -> ByteArray) {
        if (entries.contains(name)) {
            return
        }
        this.inputStream().use {
            val inputStreamSupplier = {
                transformer(readBytes()).inputStream()
            }
            val jarArchiveEntry = JarArchiveEntry(name).apply {
                method = JarArchiveEntry.DEFLATED
            }
            creator.addArchiveEntry(jarArchiveEntry, inputStreamSupplier)
        }
        entries.add(name)
    }

    fun ZipInputStream.transform(
        creator: ParallelScatterZipCreator,
        entryFactory: (ZipEntry) -> ZipArchiveEntry = ::ZipArchiveEntry,
        transformer: (ByteArray) -> ByteArray
    ) {
        while (true) {
            val next = nextEntry ?: break
            val entry = next.takeIf(::isValidate) ?: continue
            if (!entries.contains(entry.name)) {
                entries.add(entry.name)
                val zae = entryFactory(entry)
                val data = readBytes()
                val stream = InputStreamSupplier {
                    if (entry.isDirectory) {
                        data.inputStream()
                    } else {
                        transformer(data).inputStream()
                    }
                }
                creator.addArchiveEntry(zae, stream)
            }
        }
    }


    private fun isValidate(entry: ZipEntry): Boolean {
        return (entry.isDirectory || entry.name.endsWith(".class")) && !entry.name.startsWith("META-INF/")
    }
}


