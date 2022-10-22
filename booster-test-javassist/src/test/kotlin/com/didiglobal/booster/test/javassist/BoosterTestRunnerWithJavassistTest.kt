package com.didiglobal.booster.test.javassist

import org.junit.Rule
import org.junit.contrib.java.lang.system.SystemOutRule
import org.junit.runner.RunWith
import kotlin.test.Ignore
import kotlin.test.Test
import kotlin.test.assertTrue

@RunWith(BoosterTestRunnerWithJavassist::class)
class BoosterTestRunnerWithJavassistTest {

    @get:Rule
    val systemOutRule = SystemOutRule().enableLog()

    @Test
    @Ignore
    fun `test booster runner with javassist`() {
        assertTrue {
            systemOutRule.log.isNotBlank()
        }
    }

}