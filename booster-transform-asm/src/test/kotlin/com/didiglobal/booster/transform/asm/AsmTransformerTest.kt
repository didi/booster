package com.didiglobal.booster.transform.asm

import com.didiglobal.booster.annotations.Priority
import kotlin.test.Test
import kotlin.test.assertEquals

class AsmTransformerTest {

    @Test
    fun checkTransformerOrder() {
        val transformer = AsmTransformer()
        val origin = transformer.transformers
        val sorted = ArrayList(origin).sortedBy {
            it.javaClass.getAnnotation(Priority::class.java)?.value ?: 0
        }
        assertEquals(sorted, origin)
    }

}