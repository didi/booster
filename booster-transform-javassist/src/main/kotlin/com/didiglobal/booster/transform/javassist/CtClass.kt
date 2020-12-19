package com.didiglobal.booster.transform.javassist

import com.didiglobal.booster.kotlinx.execute
import com.didiglobal.booster.kotlinx.stdout
import javassist.CtClass

fun CtClass.textify(): String = "javap -c -cp ${classPool.classpath} $name".execute().stdout
