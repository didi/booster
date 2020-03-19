package com.didiglobal.booster.transform

import java.io.Closeable

/**
 * Represents a class pool
 *
 * @author johnsonlee
 */
interface KlassPool : Closeable {

    /**
     * Returns the parent
     */
    val parent: KlassPool?

    /**
     * Returns the class loader
     */
    val classLoader: ClassLoader

    /**
     * Returns an instance [Klass]
     *
     * @param type the qualified name of class
     */
    operator fun get(type: String): Klass

}
