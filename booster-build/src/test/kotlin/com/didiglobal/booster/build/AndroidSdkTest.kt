package com.didiglobal.booster.build

import kotlin.test.Test
import kotlin.test.assertNotNull

class AndroidSdkTest {

    @Test
    fun `test get location`() {
        val sdk = AndroidSdk.getLocation()
        println(sdk)
        assertNotNull(sdk)
    }

}