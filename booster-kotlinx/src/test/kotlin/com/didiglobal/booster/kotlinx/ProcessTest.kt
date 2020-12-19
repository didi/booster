package com.didiglobal.booster.kotlinx

import kotlin.test.Test
import kotlin.test.assertNotNull

class ProcessTest {

    @Test
    fun `test execute`() {
        val output = "java -showversion".execute().stderr
        print(output)
        assertNotNull(output)
    }

}