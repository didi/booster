package com.didiglobal.booster.kotlinx

import kotlin.test.Test
import kotlin.test.assertEquals

class TypesTest {

    @Test
    fun `test get descriptor`() {
        assertEquals("Z", Boolean::class.javaPrimitiveType!!.descriptor)
        assertEquals("B", Byte::class.javaPrimitiveType!!.descriptor)
        assertEquals("C", Char::class.javaPrimitiveType!!.descriptor)
        assertEquals("S", Short::class.javaPrimitiveType!!.descriptor)
        assertEquals("I", Int::class.javaPrimitiveType!!.descriptor)
        assertEquals("F", Float::class.javaPrimitiveType!!.descriptor)
        assertEquals("J", Long::class.javaPrimitiveType!!.descriptor)
        assertEquals("D", Double::class.javaPrimitiveType!!.descriptor)
        assertEquals("V", Void.TYPE.descriptor)
        assertEquals("Ljava/lang/String;", String::class.java.descriptor)
        assertEquals("[Ljava/lang/Boolean;", Array<Boolean>::class.java.descriptor)
        assertEquals("[[Ljava/lang/Boolean;", Array<Array<Boolean>>::class.java.descriptor)
    }

}