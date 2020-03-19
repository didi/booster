package com.didiglobal.booster.task.analyser

import com.didiglobal.booster.kotlinx.Wildcard
import java.net.URL
import kotlin.test.Test
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class AnalyserTaskTest {

    @Test
    fun `check if blacklist exists`() {
        assertNotNull(VALUE_BLACKLIST)
        assertTrue(URL(VALUE_BLACKLIST).openStream().bufferedReader().use {
            it.readLines().map(Wildcard.Companion::valueOf).toSet()
        }.isNotEmpty())
    }

}