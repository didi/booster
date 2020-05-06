package com.didiglobal.booster.transform

import java.io.File
import java.net.URLClassLoader

/**
 * Represents an abstraction of [KlassPool]
 *
 * @author johnsonlee
 */
abstract class AbstractKlassPool(private val classpath: Collection<File>, final override val parent: KlassPool? = null) : KlassPool {

    private val classes = mutableMapOf<String, Klass>()

    protected val imports = mutableMapOf<String, Collection<String>>()

    override val classLoader: ClassLoader = URLClassLoader(classpath.map { it.toURI().toURL() }.toTypedArray(), parent?.classLoader)

    override operator fun get(type: String) = normalize(type).let { name ->
        classes.getOrDefault(name, findClass(name))
    }

    override fun close() {
        val classLoader = this.classLoader
        if (classLoader is URLClassLoader) {
            classLoader.close()
        }
    }

    override fun toString() = "classpath: $classpath"

    internal fun getImports(name: String): Collection<String> = this.imports[name] ?: this.parent?.let { it ->
        if (it is AbstractKlassPool) it.getImports(name) else null
    } ?: emptyList()

    internal fun findClass(name: String): Klass {
        return try {
            LoadedKlass(this, classLoader.loadClass(name)).also {
                classes[name] = it
            }
        } catch (e: Throwable) {
            DefaultKlass(name)
        }
    }

}

private class DefaultKlass(name: String) : Klass {

    override val qualifiedName: String = name

    override fun isAssignableFrom(type: String) = false

    override fun isAssignableFrom(klass: Klass) = klass.qualifiedName == this.qualifiedName

}

private class LoadedKlass(val pool: AbstractKlassPool, val clazz: Class<out Any>) : Klass {

    override val qualifiedName: String = clazz.name

    override fun isAssignableFrom(type: String) = isAssignableFrom(pool.findClass(normalize(type)))

    override fun isAssignableFrom(klass: Klass) = klass is LoadedKlass && clazz.isAssignableFrom(klass.clazz)

}

private fun normalize(type: String) = if (type.contains('/')) {
    type.replace('/', '.')
} else {
    type
}