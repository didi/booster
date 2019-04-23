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
    fun `archive current dir as a jar`() {
        val pwd = File(System.getProperty("user.dir"))
        val count = pwd.walk().filter { it != pwd && it.isFile }.count()
        assertEquals(count, pwd.jar().entries().asSequence().count())
    }

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
