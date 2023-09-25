package com.didiglobal.booster.gradle

import com.didiglobal.booster.task.spi.VariantProcessor
import com.didiglobal.booster.transform.Transformer
import org.gradle.api.Project
import java.net.URL
import java.nio.charset.StandardCharsets
import java.util.ServiceConfigurationError

internal interface ServiceLoader<T> {
    fun load(vararg args: Any): List<T>
}

@Suppress("UNCHECKED_CAST")
private class ServiceLoaderImpl<T>(
        private val classLoader: ClassLoader,
        private val service: Class<T>,
        private vararg val types: Class<*>
) : ServiceLoader<T> {

    private val name = "META-INF/services/${service.name}"

    override fun load(vararg args: Any): List<T> {
        return lookup<T>().map { provider ->
            try {
                try {
                    provider.getConstructor(*types).newInstance(*args) as T
                } catch (e: NoSuchMethodException) {
                    provider.getDeclaredConstructor().newInstance() as T
                }
            } catch (e: Throwable) {
                throw ServiceConfigurationError("Provider $provider not found")
            }
        }
    }

    fun <T> lookup(): Set<Class<T>> {
        return classLoader.getResources(name)?.asSequence()?.map(::parse)?.flatten()?.toSet()?.mapNotNull { provider ->
            try {
                val providerClass = Class.forName(provider, false, classLoader)
                if (!service.isAssignableFrom(providerClass)) {
                    throw ServiceConfigurationError("Provider $provider not a subtype")
                }
                providerClass as Class<T>
            } catch (e: Throwable) {
                null
            }
        }?.toSet() ?: emptySet()
    }
}

internal class ServiceLoaderFactory<T>(private val classLoader: ClassLoader, private val service: Class<T>) {
    fun newServiceLoader(vararg types: Class<*>): ServiceLoader<T> = ServiceLoaderImpl(classLoader, service, *types)
}

internal inline fun <reified T> newServiceLoader(classLoader: ClassLoader, vararg types: Class<*>): ServiceLoader<T> {
    return ServiceLoaderFactory(classLoader, T::class.java).newServiceLoader(*types)
}

/**
 * Load [Transformer]s with the specified [classLoader]
 */
@Throws(ServiceConfigurationError::class)
internal fun lookupTransformers(classLoader: ClassLoader): Set<Class<Transformer>> {
    return ServiceLoaderImpl(classLoader, Transformer::class.java, ClassLoader::class.java).lookup()
}

/**
 * Load [VariantProcessor]s with the specified [project]
 */
@Throws(ServiceConfigurationError::class)
internal fun loadVariantProcessors(project: Project): List<VariantProcessor> {
    return newServiceLoader<VariantProcessor>(project.buildscript.classLoader, Project::class.java).load(project)
}

/**
 * Load [VariantProcessor]s with the specified [variant]
 */
@Throws(ServiceConfigurationError::class)
internal fun loadTransformers(classLoader: ClassLoader): List<Transformer> {
    return newServiceLoader<Transformer>(classLoader, ClassLoader::class.java).load(classLoader)
}

@Throws(ServiceConfigurationError::class)
private fun parse(u: URL) = try {
    u.openStream().bufferedReader(StandardCharsets.UTF_8).readLines().filter {
        it.isNotEmpty() && it.isNotBlank() && !it.startsWith('#')
    }.map(String::trim).filter(::isJavaClassName)
} catch (e: Throwable) {
    emptyList()
}

private fun isJavaClassName(text: String): Boolean {
    if (!Character.isJavaIdentifierStart(text[0])) {
        throw ServiceConfigurationError("Illegal provider-class name: $text")
    }

    for (i in 1 until text.length) {
        val cp = text.codePointAt(i)
        if (!Character.isJavaIdentifierPart(cp) && cp != '.'.code) {
            throw ServiceConfigurationError("Illegal provider-class name: $text")
        }
    }

    return true
}
