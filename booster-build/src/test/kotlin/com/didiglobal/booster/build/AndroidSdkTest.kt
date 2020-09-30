package com.didiglobal.booster.build

import kotlin.test.Test

class AndroidSdkTest {

    @Test
    fun `test get location`() {
        try {
            val sdk = AndroidSdk.location
            println("Android SDK: $sdk")
        } catch (e: RuntimeException) {
            println("Android SDK: Not Found")
        }
    }

}