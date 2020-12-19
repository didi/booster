package com.didiglobal.booster.transform.asm

import com.didiglobal.booster.build.AndroidSdk
import java.io.InputStream
import java.util.jar.JarFile
import kotlin.test.Test

class ClassNodeTest {

    @Test
    fun `test textify`() {
        JarFile(AndroidSdk.getAndroidJar()).use { jar ->
            val clazz = jar.getInputStream(jar.getJarEntry("android/content/Intent.class")).use(InputStream::asClassNode)
            print(clazz.textify())
        }
    }

}