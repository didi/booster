package com.didiglobal.booster.command

import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class InstalledCommandTest {

    @Test
    fun `test java`() {
        val java = CommandService.fromPath("java")!!
        assertEquals("java", java.name)
        assertTrue(java.install(File(System.getProperty("user.dir"), "build${File.separator}bin${File.separator}java")))
        println(java.location)
        println(java.executable)
        java.location.openStream().use {
            assertTrue(it.available() > 0)
        }
    }

}
