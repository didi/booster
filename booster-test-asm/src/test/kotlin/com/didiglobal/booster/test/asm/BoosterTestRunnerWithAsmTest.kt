package com.didiglobal.booster.test.asm

import org.junit.Rule
import org.junit.contrib.java.lang.system.SystemOutRule
import org.junit.runner.RunWith
import kotlin.test.Ignore
import kotlin.test.Test
import kotlin.test.assertTrue

@RunWith(BoosterTestRunnerWithAsm::class)
class BoosterTestRunnerWithAsmTest {

    @get:Rule
    val systemOutRule = SystemOutRule().enableLog()

    @Test
    @Ignore
    fun `test booster runner with asm`() {
        assertTrue {
            systemOutRule.log.isNotBlank()
        }
    }

}