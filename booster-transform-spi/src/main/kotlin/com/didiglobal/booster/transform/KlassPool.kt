package com.didiglobal.booster.transform

/**
 * Represents a class pool
 *
 * @author johnsonlee
 */
interface KlassPool {

    /**
     * Returns an instance [Klass]
     *
     * @param name the qualified name of class
     */
    fun get(name: String): Klass

}
