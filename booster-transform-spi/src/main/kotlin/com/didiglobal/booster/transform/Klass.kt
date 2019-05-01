package com.didiglobal.booster.transform

/**
 * Represents a mirror of a specific class
 *
 * @author johnsonlee
 */
interface Klass {

    /**
     * The qualified name of class
     */
    val qualifiedName: String

    /**
     * Tests if this class is assignable from the specific type
     *
     * @param type the qualified name of type
     */
    fun isAssignableFrom(type: String): Boolean

    /**
     * Tests if this class is assignable from the specific type
     *
     * @param klass the [Klass] object to be checked
     */
    fun isAssignableFrom(klass: Klass): Boolean

}
