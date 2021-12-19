package com.didiglobal.booster.transform.util

import com.didiglobal.booster.transform.AbstractTransformContext
import com.didiglobal.booster.transform.TransformContext
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

    private val context: TransformContext by lazy {
        object : AbstractTransformContext(javaClass.name, javaClass.name, emptyList(), classpath, classpath) {}
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
                getResourceAsStream("${name.replace('.', '/')}.class")?.use(InputStream::readBytes)?.let {
                    transform(context, it)
                } ?: throw IOException("Read class $name failed")
            } finally {
                onPostTransform(context)
            }
        }

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

