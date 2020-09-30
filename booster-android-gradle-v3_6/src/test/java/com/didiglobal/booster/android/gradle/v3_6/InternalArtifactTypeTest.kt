package com.didiglobal.booster.android.gradle.v3_6

import kotlin.test.Test

class InternalArtifactTypeTest {

    @Test
    fun test() {
        ARTIFACT_TYPES.forEach { (k, v) ->
            println("$k => $v")
        }
    }
}