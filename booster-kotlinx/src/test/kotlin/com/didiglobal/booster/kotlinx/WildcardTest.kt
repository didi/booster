package com.didiglobal.booster.kotlinx

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class WildcardTest {

    @Test
    fun `wildcard matching`() {
        assertFalse("java/io/File.exists()B".matches(Wildcard("java/x/**")))
        assertTrue("java/io/File.exists()B".matches(Wildcard("java/io/*")))
    }

}
