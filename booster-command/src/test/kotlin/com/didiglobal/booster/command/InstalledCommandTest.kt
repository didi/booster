package com.didiglobal.booster.command

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class InstalledCommandTest {

    @Test
    fun `test java`() {
        val java = CommandService.fromPath("java")
        assertNotNull(java)
        assertEquals("java", java.name)
        println(java.location)
        java.location.openStream().use {
            assertTrue(it.available() > 0)
        }
    }

}
