package com.didiglobal.booster.task.profile.dot

import com.didiglobal.booster.task.profile.dot.toPrettyString
import com.didiglobal.booster.task.profile.graph.CallGraph
import kotlin.test.Test

class DirectedCallGraphPrinterTest {

    @Test
    fun `prettify node`() {
        println(CallGraph.Node.valueOf("android/app/Activity.onCreate(Landroid/os/Bundle;Landroid/os/PersistableBundle;)V").toPrettyString())
    }

}
