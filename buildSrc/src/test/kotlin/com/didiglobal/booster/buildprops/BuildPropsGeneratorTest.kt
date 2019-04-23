package com.didiglobal.booster.buildprops

import kotlin.test.Test
import kotlin.test.assertEquals

class BuildPropsGeneratorTest {

    @Test
    fun test_mkpkg() {
        val s = mkpkg("booster.booster-gradle-plugin")
        assertEquals("booster.gradle.plugin", s)
    }

}
