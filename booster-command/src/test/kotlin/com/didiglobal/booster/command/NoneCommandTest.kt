package com.didiglobal.booster.command

import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse

class NoneCommandTest {

    @Test
    fun `test install`() {
        val fake = NoneCommand("fake")
        assertEquals("fake", fake.name)
        assertFalse(fake.install(File(System.getProperty("user.dir"), "build${File.separator}bin${File.separator}fake")))
        println(fake.location)
        println(fake.executable)
        fake.location.openStream().use {
            assertEquals(0, it.available())
        }
    }

}