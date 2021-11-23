package com.didiglobal.booster.transform.util

import com.didiglobal.booster.transform.AbstractTransformContext
import com.didiglobal.booster.transform.TransformContext
import com.didiglobal.booster.transform.Transformer
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.net.URLClassLoader

class TransformerClassLoader : URLClassLoader {

    private val transformer: Transformer

    constructor(
            delegate: URLClassLoader,
            factory: (ClassLoader) -> Transformer
    ) : super(delegate.urLs) {
        this.transformer = factory(this)
    }

    constructor(
            delegate: URLClassLoader,
            factory: (ClassLoader, Iterable<Transformer>) -> Transformer,
            vararg transformer: Transformer
    ) : super(delegate.urLs) {
        this.transformer = factory(this, transformer.asIterable())
    }

    private val classpath: Collection<File> by lazy {
        this.urLs.map { File(it.path) }
    }

    private val context: TransformContext by lazy {
        object : AbstractTransformContext(javaClass.name, javaClass.name, classpath, classpath) {}
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
