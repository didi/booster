package com.didiglobal.booster.cha.graph.dot

import com.didiglobal.booster.cha.graph.CallGraph
import kotlin.test.Test

class DirectedCallGraphPrinterTest {

    @Test
    fun `prettify node`() {
        println(CallGraph.Node.valueOf("android/app/Activity.onCreate(Landroid/os/Bundle;Landroid/os/PersistableBundle;)V").toPrettyString())
    }

}
