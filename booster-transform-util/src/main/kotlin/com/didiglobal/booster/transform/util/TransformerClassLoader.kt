package com.didiglobal.booster.transform.util

import com.didiglobal.booster.kotlinx.file
import com.didiglobal.booster.kotlinx.search
import com.didiglobal.booster.kotlinx.touch
import com.didiglobal.booster.transform.AbstractTransformContext
import com.didiglobal.booster.transform.Transformer
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.net.URL
import java.net.URLClassLoader

typealias ClassFilter = (Class<*>) -> Boolean

internal val DEFAULT_CLASS_FILTER: ClassFilter = {
    it.classLoader == null
            || it.name.startsWith("java.")
            || it.name.startsWith("kotlin.")
            || it.name.startsWith("org.junit.")
}

class TransformerClassLoader : URLClassLoader {

    private val transformer: Transformer

    private val filter: ClassFilter

    constructor(
            delegate: ClassLoader,
            filter: ClassFilter = DEFAULT_CLASS_FILTER,
            factory: (ClassLoader) -> Transformer
    ) : super(delegate.classpath) {
        this.filter = filter
        this.transformer = factory(this)
    }

    constructor(
            delegate: ClassLoader,
            filter: ClassFilter = DEFAULT_CLASS_FILTER,
            factory: (ClassLoader, Iterable<Transformer>) -> Transformer,
            vararg transformer: Transformer
    ) : super(delegate.classpath) {
        this.filter = filter
        this.transformer = factory(this, transformer.asIterable())
    }

    private val classpath: Collection<File> by lazy {
        this.urLs.map { File(it.path) }
    }

    private val dirs: Collection<File> by lazy {
        classpath.filter(File::isDirectory).toSortedSet()
    }

    private val buildType: String by lazy {
        dirs.find {
            it.name.endsWith("UnitTest")
        }?.name?.substringBeforeLast("UnitTest") ?: dirs.hashCode().toString()
    }

    private val cwd: File = File(System.getProperty("user.dir"))

    private val output = cwd.file("build", "tmp", "booster", buildType)

    private val classes: Set<String> by lazy {
        dirs.map { base ->
            val baseUri = base.toURI()

            base.search {
                it.extension == "class"
            }.map {
                val path = baseUri.relativize(it.toURI()).path
                path.substringBeforeLast(".class").replace('/', '.')
            }
        }.flatten().toSet()
    }

    private val context by lazy {
        object : AbstractTransformContext(cwd.name, buildType, emptyList(), classpath, classpath) {}
    }

    override fun loadClass(name: String, resolve: Boolean): Class<*> {
        return super.loadClass(name, resolve)?.takeIf {
            DEFAULT_CLASS_FILTER(it) || this.filter(it)
        } ?: synchronized(getClassLoadingLock(name)) {
            (findLoadedClass(name) ?: findClass(name) ?: parent.loadClass(name)).apply {
                resolveClass(this)
            }
        }
    }

    override fun findClass(name: String): Class<*> {
        val bytecode = transformer.run {
            try {
                onPreTransform(context)
                context.collect()
                getResourceAsStream("${name.replace('.', '/')}.class")?.use(InputStream::readBytes)?.let {
                    transform(context, it)
                } ?: throw IOException("Read class $name failed")
            } finally {
                onPostTransform(context)
            }
        }

        /* See: https://groups.google.com/g/jacoco/c/RDc5rJdKsfA/m/l701T8YjAAAJ
         * Classes in bundle 'xxx' do no match with execution data.
         *
         * Writing the manipulated bytecode into the build/tmp/booster/{buildType} directory
         * So that, the output can be used by Jacoco to generate the coverage report
         *
         * For Jacoco report task, the classes directory should be configured as following:
         *
         * ```kotlin
         * getAndroid<LibraryExtension>().let { android ->
         *     afterEvaluate {
         *         android.libraryVariants.forEach { variant ->
         *             tasks.register("jacocoTestReportFor${variant.name.capitalize()}", JacocoReport::class) {
         *                 reports {
         *                     xml.isEnabled = false
         *                     html.isEnabled = true
         *                 }
         *
         *                 sourceDirectories.setFrom(files(variant.sourceSets.map { it.javaDirectories }.flatten()))
         *                 classDirectories.setFrom(files("${buildDir}/tmp/booster/${variant.name}"))
         *                 executionData.setFrom(files("${buildDir}/jacoco/test${variant.name.capitalize()}UnitTest.exec"))
         *             }.apply {
         *                 dependsOn("test${variant.name.capitalize()}UnitTest")
         *             }
         *         }
         *     }
         * }
         * ```
         */
        File(output, name.replace('.', File.separatorChar)).takeIf {
            name in classes
        }?.touch()?.writeBytes(bytecode)

        return defineClass(name, bytecode, 0, bytecode.size)
    }

}

private val ClassLoader.classpath: Array<URL>
    get() = (this as? URLClassLoader)?.urLs ?: System.getProperty("java.class.path").split(File.pathSeparatorChar).map {
        try {
            File(it).toURI().toURL()
        } catch (e: SecurityException) {
            URL("file", null, File(it).absolutePath)
        }
    }.toTypedArray()

