package com.didiglobal.booster.transform.util

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ClassNameCollectorTest {

    @Test
    fun `collect all class names`() {
        assertFalse(Collectors.ClassNameCollector.accept("a"))
        assertTrue(Collectors.ClassNameCollector.accept("a.class"))
        assertEquals("a", Collectors.ClassNameCollector.collect("a.class") {
            byteArrayOf()
        })
        assertEquals("a.b.c", Collectors.ClassNameCollector.collect("a/b/c.class") {
            byteArrayOf()
        })
        assertEquals("a.b.c$1", Collectors.ClassNameCollector.collect("a/b/c$1.class") {
            byteArrayOf()
        })
    }

}