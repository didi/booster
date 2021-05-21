package com.didiglobal.booster.cha

import java.io.File
import java.io.InputStream

internal object NullClassFileParser : ClassFileParser<Any?> {
    override fun parse(file: File): Any? = null
    override fun getClassName(classNode: Any?): String = ""
    override fun parse(input: InputStream): Any? = null
    override fun getSuperName(classNode: Any?): String? = null
    override fun getInterfaces(classNode: Any?): Array<String> = emptyArray()
    override fun getAccessFlags(classNode: Any?): Int = 0
}