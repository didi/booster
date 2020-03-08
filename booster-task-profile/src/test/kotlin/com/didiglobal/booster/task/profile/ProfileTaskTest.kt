package com.didiglobal.booster.task.profile

import com.didiglobal.booster.kotlinx.Wildcard
import java.net.URL
import kotlin.test.Test
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class ProfileTaskTest {

    @Test
    fun `check if lint-apis exists`() {
        assertNotNull(DEFAULT_APIS)
        assertTrue(URL(DEFAULT_APIS).openStream().bufferedReader().use {
            it.readLines().map(Wildcard.Companion::valueOf).toSet()
        }.isNotEmpty())
    }

}