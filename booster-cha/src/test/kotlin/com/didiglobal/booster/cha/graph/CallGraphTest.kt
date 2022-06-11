package com.didiglobal.booster.cha.graph

import kotlin.test.Test
import kotlin.test.assertEquals

class CallGraphTest {

    @Test
    fun `check node args`() {
        assertEquals("", CallNode("java/lang/Object", "wait", "()V").args)
        assertEquals("II", CallNode("java/lang/String", "substring", "(II)Ljava/lang/String;").args)
    }

}