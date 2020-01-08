package com.didiglobal.booster.transform.util

import java.io.InputStream
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test

class ImportAnalyserTest {

    private lateinit var bytecode: InputStream

    @BeforeTest
    fun setup() {
        bytecode = javaClass.classLoader.getResourceAsStream("Configuration.class")!!
    }

    @Test
    fun analyse() {
        ClassFileSnapshot(bytecode).imports.forEach(::println)
    }

    @AfterTest
    fun teardown() {
        bytecode.close()
    }

}