package com.didiglobal.booster.kotlinx

import java.io.File
import java.math.BigInteger
import java.security.MessageDigest
import java.util.regex.Pattern

private val PATTERN_JAVA_IDENTIFIER = Pattern.compile("[a-zA-Z_\$][a-zA-Z\\d_\$]*")

fun String.separatorsToUnix(): String {
    return if ('/' == File.separatorChar) this else this.replace(File.separatorChar, '/')
}

fun String.separatorsToSystem(): String {
    return if ('/' != File.separatorChar && this.contains('/')) this.replace('/', File.separatorChar) else this
}

fun String.md5(): String {
    return BigInteger(1, MessageDigest.getInstance("MD5").digest(toByteArray())).toString(16).padStart(32, '0')
}

fun String.matches(wildcard: Wildcard): Boolean {
    return wildcard.matches(this)
}

fun String.isValidJavaIdentifier(): Boolean = PATTERN_JAVA_IDENTIFIER.matcher(this).matches()
