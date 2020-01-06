package com.didiglobal.booster.task.compression

import com.didiglobal.booster.compression.isFlatPng
import com.didiglobal.booster.compression.isPng
import java.io.File
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class FileNameTest {

    @Test
    fun `test png name`() {
        assertTrue(isPng(File("abc_text.png")))
        assertFalse(isPng(File("abc_text.9.png")))

        assertTrue(isFlatPng(File("drawable/abc_text.png.flat")))
        assertFalse(isFlatPng(File("drawable/abc_text.9.png.flat")))
    }

}
