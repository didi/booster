package com.didiglobal.booster.gradle

import kotlin.test.Test

class InternalArtifactTypeTest {

    @Test
    fun test() {
        ARTIFACT_TYPES.forEach { (k, v) ->
            println("$k => $v")
        }
    }
}