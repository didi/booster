package com.didiglobal.booster.build

import kotlin.test.Test
import kotlin.test.assertNotNull

class AndroidSdkTest {

    @Test(expected = RuntimeException::class)
    fun `test get location`() {
        val sdk = AndroidSdk.getLocation()
        assertNotNull(sdk)
        println("Android SDK: $sdk")
    }

}