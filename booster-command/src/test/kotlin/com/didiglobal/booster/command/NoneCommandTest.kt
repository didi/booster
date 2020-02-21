package com.didiglobal.booster.command

import kotlin.test.Test
import kotlin.test.assertEquals

class NoneCommandTest {

    @Test
    fun `test install`() {
        val fake = NoneCommand("fake")
        assertEquals("fake", fake.name)
        println(fake.location)
        fake.location.openStream().use {
            assertEquals(0, it.available())
        }
    }

}