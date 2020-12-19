package com.didiglobal.booster.kotlinx

import java.io.BufferedReader

fun String.execute(): Process = Runtime.getRuntime().exec(this)

val Process.stdout: String
    get() = inputStream.bufferedReader().use(BufferedReader::readText)

val Process.stderr: String
    get() = errorStream.bufferedReader().use(BufferedReader::readText)
