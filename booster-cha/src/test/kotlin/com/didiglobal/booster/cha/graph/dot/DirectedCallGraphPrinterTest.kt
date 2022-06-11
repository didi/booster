package com.didiglobal.booster.cha.graph.dot

import com.didiglobal.booster.cha.graph.CallNode
import kotlin.test.Test

class DirectedCallGraphPrinterTest {

    @Test
    fun `prettify node`() {
        println(CallNode.valueOf("android/app/Activity.onCreate(Landroid/os/Bundle;Landroid/os/PersistableBundle;)V").toPrettyString())
    }

}
