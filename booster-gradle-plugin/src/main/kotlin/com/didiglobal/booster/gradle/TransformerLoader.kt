package com.didiglobal.booster.gradle

import com.didiglobal.booster.transform.Transformer
import java.net.URL
import java.nio.charset.StandardCharsets
import java.util.ServiceConfigurationError

private const val PREFIX = "META-INF/services/"

/**
 * Load [Transformer]s with the specified [classLoader]
 */
@Throws(ServiceConfigurationError::class)
internal fun loadTransformers(classLoader: ClassLoader): List<Transformer> = classLoader.getResources(PREFIX + Transformer::class.java.name)?.asSequence()?.map(::parse)?.flatten()?.toSet()?.mapNotNull { provider ->
    try {
        val transformerClass = Class.forName(provider, false, classLoader)
        if (!Transformer::class.java.isAssignableFrom(transformerClass)) {
            throw ServiceConfigurationError("Provider $provider not a subtype")
        }

        try {
            transformerClass.getConstructor(ClassLoader::class.java).newInstance(classLoader) as Transformer
        } catch (e: NoSuchMethodException) {
            transformerClass.newInstance() as Transformer
        }
    } catch (e: ClassNotFoundException) {
        throw ServiceConfigurationError("Provider $provider not found")
    }
} ?: emptyList()

@Throws(ServiceConfigurationError::class)
private fun parse(u: URL) = try {
    u.openStream().bufferedReader(StandardCharsets.UTF_8).readLines().filter {
        it.isNotEmpty() && it.isNotBlank() && !it.startsWith('#')
    }.map(String::trim).filter(::isJavaClassName)
} catch (e: Throwable) {
    emptyList<String>()
}

private fun isJavaClassName(text: String): Boolean {
    if (!Character.isJavaIdentifierStart(text[0])) {
        throw ServiceConfigurationError("Illegal provider-class name: $text")
    }

    for (i in 1 until text.length) {
        val cp = text.codePointAt(i)
        if (!Character.isJavaIdentifierPart(cp) && cp != '.'.toInt()) {
            throw ServiceConfigurationError("Illegal provider-class name: $text")
        }
    }

    return true
}
