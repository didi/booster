package com.didiglobal.booster.cha.graph

import com.didiglobal.booster.cha.graph.CallGraph
import kotlin.test.Test
import kotlin.test.assertEquals

class CallGraphTest {

    @Test
    fun `check node args`() {
        assertEquals("", CallGraph.Node("java/lang/Object", "wait", "()V").args)
        assertEquals("II", CallGraph.Node("java/lang/String", "substring", "(II)Ljava/lang/String;").args)
    }

}