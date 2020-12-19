package com.didiglobal.booster.transform.javassist

import com.didiglobal.booster.build.AndroidSdk
import javassist.ClassPool
import kotlin.test.Test

class CtClassTest {

    @Test
    fun `test textify`() {
        val pool = ClassPool().apply {
            appendClassPath(AndroidSdk.getAndroidJar().canonicalPath)
        }
        print(pool.get("android.content.Intent").textify())
    }

}