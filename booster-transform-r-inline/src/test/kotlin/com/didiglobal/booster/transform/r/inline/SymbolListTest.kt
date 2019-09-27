package com.didiglobal.booster.transform.r.inline

import com.didiglobal.booster.kotlinx.file
import java.io.File
import kotlin.test.Test

val PWD = File(System.getProperty("user.dir"))

class SymbolListTest {

    @Test
    fun `parse symbol list from R txt`() {
        SymbolList.from(PWD.file("src", "test", "resources", "R.txt")).forEach { symbol ->
            println(symbol)
        }
    }

}
