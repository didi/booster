package com.didiglobal.booster.transform.lint

import com.didiglobal.booster.kotlinx.Wildcard
import com.didiglobal.booster.transform.asm.AsmTransformer
import com.didiglobal.booster.transform.util.TransformHelper
import java.io.File
import java.net.URL
import kotlin.test.Test
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class LintTransformerTest {

    fun test() {
        val input = File(javaClass.classLoader.getResource("AnnotationExample1.class")!!.file).parentFile
        val helper = TransformHelper(input, AsmTransformer(LintTransformer()))
        helper.transform()
        println(helper.output)
    }

    @Test
    fun `check if lint-apis exists`() {
        assertNotNull(DEFAULT_APIS)
        assertTrue(URL(DEFAULT_APIS).openStream().bufferedReader().use {
            it.readLines().map(Wildcard.Companion::valueOf).toSet()
        }.isNotEmpty())
    }

}