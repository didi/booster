package com.didiglobal.booster.transform.br.inline

import com.didiglobal.booster.kotlinx.file
import java.io.File
import kotlin.test.Test

val PWD = File(System.getProperty("user.dir"))

class SymbolListTest {

    @Test
    fun `parse symbol list from BR class`() {
        SymbolList.from(PWD.file("src", "test", "resources", "BR.class")).forEach { symbol ->
            println(symbol)
        }
    }

}
