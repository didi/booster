package com.didiglobal.booster.kotlinx

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Unit test for String extension
 *
 * @author johnsonlee
 */
class TextTest {

    @Test
    fun `test java identifier validation`() {
        assertFalse("".isValidJavaIdentifier())
        assertFalse("1".isValidJavaIdentifier())
        assertFalse(".".isValidJavaIdentifier())
        assertTrue("_".isValidJavaIdentifier())
        assertTrue("$".isValidJavaIdentifier())
        assertFalse("1a".isValidJavaIdentifier())
        assertFalse(".a".isValidJavaIdentifier())
        assertTrue("_a".isValidJavaIdentifier())
        assertTrue("\$a".isValidJavaIdentifier())
        assertTrue("ab".isValidJavaIdentifier())
        assertTrue("ab1".isValidJavaIdentifier())
        assertTrue("ab$".isValidJavaIdentifier())
        assertTrue("ab_".isValidJavaIdentifier())
        assertFalse("ab.".isValidJavaIdentifier())
    }

}
