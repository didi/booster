package com.didiglobal.booster.transform.lint

import java.net.URI
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class URITest {

    @Test
    fun `parse string as uri`() {
        val home = URI(System.getProperty("user.home"))
        assertFalse(home.isAbsolute)
        val github = URI("http://github.com")
        assertTrue(github.isAbsolute)
    }

}
