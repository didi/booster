package com.didiglobal.booster.build

import kotlin.test.Test
import kotlin.test.assertNotNull

class AndroidSdkTest {

    @Test
    fun `test get location`() {
        try {
            val sdk = AndroidSdk.getLocation()
            println("Android SDK: $sdk")
        } catch (e: RuntimeException) {
            println("Android SDK: Not Found")
        }
    }

}