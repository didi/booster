package com.didiglobal.booster.transform.util

import com.didiglobal.booster.transform.Supervisor

/**
 * An abstraction of [Supervisor]
 *
 * @param action The action for observing
 */
abstract class AbstractSupervisor<T>(
        protected val action: (T) -> Unit
) : Supervisor

/**
 * A supervisor for class descriptor observing
 *
 * @param prefix The prefix of class descriptor
 */
open class ClassDescriptorSupervisor(
        private val prefix: String,
        action: (String) -> Unit
) : AbstractSupervisor<String>(action) {

    override fun accept(name: String): Boolean {
        return name.endsWith(".class", true) && name.startsWith(prefix)
    }

    override fun collect(name: String, data: () -> ByteArray) {
        action(name.substringBeforeLast(".class"))
    }

}

/**
 * A supervisor for class name observing
 */
class ClassNameSupervisor(
        private val prefix: String,
        action: (String) -> Unit
) : AbstractSupervisor<String>(action) {

    override fun accept(name: String): Boolean {
        return name.endsWith(".class", true) && name.startsWith(prefix)
    }

    override fun collect(name: String, data: () -> ByteArray) {
        action(name.substringBeforeLast(".class").replace('/', '.'))
    }

}

/**
 * A supervisor for service (SPI) observing
 */
class ServiceSupervisor(
        action: (Pair<String, Collection<String>>) -> Unit
) : AbstractSupervisor<Pair<String, Collection<String>>>(action) {

    override fun accept(name: String): Boolean {
        return name matches REGEX_SPI
    }

    override fun collect(name: String, data: () -> ByteArray) {
        val `interface` = REGEX_SPI.matchEntire(name)!!.groupValues[1]
        val implementations = data().inputStream().bufferedReader().lineSequence().filterNot {
            it.isBlank() || it.startsWith('#')
        }.toSet()
        action(`interface` to implementations)
    }

}
