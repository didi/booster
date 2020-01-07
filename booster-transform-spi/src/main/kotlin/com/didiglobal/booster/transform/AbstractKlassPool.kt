package com.didiglobal.booster.transform

import java.io.File
import java.net.URLClassLoader

abstract class AbstractKlassPool(val classpath: Collection<File>) : KlassPool {

    private val klasses = mutableMapOf<String, Klass>()

    private val classLoader = URLClassLoader(classpath.map { it.toURI().toURL() }.toTypedArray())

    override fun get(type: String): Klass {
        val name = normalize(type)
        return klasses.getOrDefault(name, findClass(name))
    }

    internal fun findClass(name: String): Klass {
        return try {
            LoadedKlass(this, Class.forName(name, false, classLoader)).also {
                klasses[name] = it
            }
        } catch (e: Throwable) {
            DefaultKlass(name)
        }
    }

    override fun toString(): String {
        return "classpath: $classpath"
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