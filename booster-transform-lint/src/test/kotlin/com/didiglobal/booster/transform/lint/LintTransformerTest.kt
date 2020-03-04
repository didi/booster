package com.didiglobal.booster.transform.lint

import com.didiglobal.booster.transform.asm.AsmTransformer
import com.didiglobal.booster.transform.util.TransformHelper
import java.io.File
import kotlin.test.Test

class LintTransformerTest {

    @Test
    fun test() {
        val input = File(javaClass.classLoader.getResource("AnnotationExample1.class")!!.file).parentFile
        val helper = TransformHelper(input, AsmTransformer(LintTransformer()))
        helper.transform()
        println(helper.output)
    }

}