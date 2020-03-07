package com.didiglobal.booster.kotlinx

import kotlin.test.Test
import kotlin.test.assertEquals

class TypesTest {

    @Test
    fun `test get descriptor`() {
        assertEquals("Z", Types.getDescriptor(Boolean::class.java))
        assertEquals("B", Types.getDescriptor(Byte::class.java))
        assertEquals("C", Types.getDescriptor(Char::class.java))
        assertEquals("S", Types.getDescriptor(Short::class.java))
        assertEquals("I", Types.getDescriptor(Int::class.java))
        assertEquals("F", Types.getDescriptor(Float::class.java))
        assertEquals("J", Types.getDescriptor(Long::class.java))
        assertEquals("D", Types.getDescriptor(Double::class.java))
        assertEquals("Ljava/lang/String;", Types.getDescriptor(String::class.java))
        assertEquals("[Ljava/lang/Boolean;", Types.getDescriptor(Array<Boolean>::class.java))
        assertEquals("[[Ljava/lang/Boolean;", Types.getDescriptor(Array<Array<Boolean>>::class.java))
    }

}