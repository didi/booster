package com.didiglobal.booster.task.compression.cwebp

import com.didiglobal.booster.command.CommandService
import com.didiglobal.booster.kotlinx.OS
import kotlin.test.Test
import kotlin.test.assertNotNull

class CwebpProviderTest {

    @Test
    fun `load cwebp by spi`() {
        val cwebp = CommandService.get("cwebp${OS.executableSuffix}")
        assertNotNull(cwebp)
        println(cwebp.location)
    }

}