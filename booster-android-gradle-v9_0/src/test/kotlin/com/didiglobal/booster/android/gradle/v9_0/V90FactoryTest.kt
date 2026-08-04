package com.didiglobal.booster.android.gradle.v9_0

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertSame

class V90FactoryTest {

    @Test
    fun `factory exposes AGP 9 revision and implementation`() {
        val factory = V90Factory()

        assertEquals(9, factory.revision.major)
        assertEquals(0, factory.revision.minor)
        assertSame(V90, factory.newAGPInterface())
    }
}
