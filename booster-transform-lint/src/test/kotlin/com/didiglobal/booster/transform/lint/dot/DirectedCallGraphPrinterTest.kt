package com.didiglobal.booster.transform.lint.dot

import com.didiglobal.booster.transform.lint.graph.CallGraph
import kotlin.test.Test

class DirectedCallGraphPrinterTest {

    @Test
    fun `prettify node`() {
        println(CallGraph.Node.valueOf("android/app/Activity.onCreate(Landroid/os/Bundle;Landroid/os/PersistableBundle;)V").toPrettyString())
    }

}
