package com.didiglobal.booster.transform.javassist

import javassist.ClassPool
import java.io.File

val ClassPool.classpath: String
    get() = toString()
            .split(File.pathSeparatorChar)
            .asSequence()
            .filter(String::isNotEmpty)
            .distinct()
            .map(::File)
            .filter(File::exists)
            .joinToString(File.pathSeparator, transform = File::getCanonicalPath)
