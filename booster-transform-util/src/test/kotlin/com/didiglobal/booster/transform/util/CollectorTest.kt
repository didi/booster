package com.didiglobal.booster.transform.util

import com.didiglobal.booster.transform.AbstractTransformContext
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CollectorTest {

    @Test
    fun `collect class names from jar`() {
        val classpath = listOf(File(javaClass.classLoader.getResource("pinyin4j-2.5.0.jar")!!.file))
        val context = object : AbstractTransformContext("", "", emptyList(), classpath) {}
        val classes = context.collect(Collectors.ClassNameCollector)
        println(classes)
        assertTrue(classes.isNotEmpty())
    }

    @Test
    fun `collect services from dir`() {
        val classpath = listOf(
                File(javaClass.protectionDomain.codeSource.location.file),
                File(javaClass.classLoader.getResource("pinyin4j-2.5.0.jar")!!.file).parentFile
        )
        val context = object : AbstractTransformContext("", "", emptyList(), classpath) {}
        val (api, impl) = context.collect(Collectors.ServiceCollector).single()
        assertEquals(api, Collector::class.java.name)
        assertEquals(2, impl.size)
    }

}
