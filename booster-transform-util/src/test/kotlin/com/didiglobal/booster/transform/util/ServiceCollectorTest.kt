package com.didiglobal.booster.transform.util

import com.didiglobal.booster.transform.Transformer
import com.didiglobal.booster.transform.asm.AsmTransformer
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

const val META_INF_SERVICES = "META-INF/services"

class ServiceCollectorTest {

    @Test
    fun `collect services from empty directory`() {
        assertFalse(Collectors.ServiceCollector.accept("${META_INF_SERVICES}/"))
    }

    @Test
    fun `collect services from invalid directory`() {
        assertFalse(Collectors.ServiceCollector.accept("/${META_INF_SERVICES}/${Transformer::class.java.name}"))
    }

    @Test
    fun `collect service with fqn`() {
        assertTrue(Collectors.ServiceCollector.accept("${META_INF_SERVICES}/${Transformer::class.java.name}"))
        assertEquals(Transformer::class.java.name to setOf(
                AsmTransformer::class.java.name
        ), Collectors.ServiceCollector.collect("${META_INF_SERVICES}/${Transformer::class.java.name}") {
            """
            ${AsmTransformer::class.java.name}
            """.trimIndent().toByteArray()
        })
    }

    @Test
    fun `collect service without package name`() {
        assertTrue(Collectors.ServiceCollector.accept("${META_INF_SERVICES}/Transformer"))
        assertEquals("Transformer" to setOf(
                AsmTransformer::class.java.name
        ), Collectors.ServiceCollector.collect("${META_INF_SERVICES}/Transformer") {
            """
            ${AsmTransformer::class.java.name}
            """.trimIndent().toByteArray()
        })
    }

    @Test
    fun `collect anonymous service without package name`() {
        assertTrue(Collectors.ServiceCollector.accept("${META_INF_SERVICES}/Transformer$"))
        assertEquals("Transformer$" to setOf(
                AsmTransformer::class.java.name
        ), Collectors.ServiceCollector.collect("${META_INF_SERVICES}/Transformer$") {
            """
            ${AsmTransformer::class.java.name}
            """.trimIndent().toByteArray()
        })
    }

    @Test
    fun `collect anonymous service with package name`() {
        assertTrue(Collectors.ServiceCollector.accept("${META_INF_SERVICES}/a.Transformer$"))
        assertEquals("a.Transformer$" to setOf(
                AsmTransformer::class.java.name
        ), Collectors.ServiceCollector.collect("${META_INF_SERVICES}/a.Transformer$") {
            """
            ${AsmTransformer::class.java.name}
            """.trimIndent().toByteArray()
        })
    }

    @Test
    fun `collect services`() {
        assertTrue(Collectors.ServiceCollector.accept("${META_INF_SERVICES}/_.Transformer$1"))
        assertTrue(Collectors.ServiceCollector.accept("${META_INF_SERVICES}/$.Transformer$1"))
        assertTrue(Collectors.ServiceCollector.accept("${META_INF_SERVICES}/a._.$.Transformer$1"))
        assertTrue(Collectors.ServiceCollector.accept("${META_INF_SERVICES}/a1.b1.Transformer$1"))
        assertTrue(Collectors.ServiceCollector.accept("${META_INF_SERVICES}/a1$.b1$.Transformer$1"))
        assertTrue(Collectors.ServiceCollector.accept("${META_INF_SERVICES}/a1_.b1_.Transformer$1"))
        assertTrue(Collectors.ServiceCollector.accept("${META_INF_SERVICES}/$1a.Transformer$1"))
        assertTrue(Collectors.ServiceCollector.accept("${META_INF_SERVICES}/_1a.Transformer$1"))
        assertFalse(Collectors.ServiceCollector.accept("${META_INF_SERVICES}/a/Transformer$1"))
        assertFalse(Collectors.ServiceCollector.accept("${META_INF_SERVICES}/-.Transformer$1"))
        assertFalse(Collectors.ServiceCollector.accept("${META_INF_SERVICES}/1a.Transformer$1"))
        assertFalse(Collectors.ServiceCollector.accept("${META_INF_SERVICES}/a1_$-.Transformer$1"))
    }


}