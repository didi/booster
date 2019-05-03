package com.didiglobal.booster.kotlinx

import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Unit test for File extension
 *
 * @author johnsonlee
 */
class FileTest {

    @Test
    fun `read the 1st line of file`() {
        val head = "Hello, booster!"
        val file = File.createTempFile("FileTest-", ".txt")

        file.printWriter().use {
            it.println(head)
        }
        assertEquals(head, file.head())
    }

}
