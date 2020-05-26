package com.didiglobal.booster.kotlinx

import java.lang.reflect.Method

private val SIGNATURES = mapOf(
        Boolean::class.java to "Z",
        Byte::class.java    to "B",
        Char::class.java    to "C",
        Short::class.java   to "S",
        Int::class.java     to "I",
        Float::class.java   to "F",
        Double::class.java  to "D",
        Long::class.java    to "J",
        Void.TYPE           to "V"
)

val Class<*>.descriptor: String
    get() {
        val signature = SIGNATURES[this]
        if (null != signature) {
            return signature
        }
        return if (isArray) {
            "[" + componentType.descriptor
        } else "L" + name.replace('.', '/') + ";"
    }

val Method.descriptor: String
    get() = parameters.joinToString("", "(", ")${returnType.descriptor}") {
        it.type.descriptor
    }
