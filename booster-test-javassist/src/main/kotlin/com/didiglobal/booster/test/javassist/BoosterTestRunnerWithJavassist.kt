package com.didiglobal.booster.test.javassist

import com.didiglobal.booster.test.BoosterTestRunner
import com.didiglobal.booster.transform.javassist.ClassTransformer
import com.didiglobal.booster.transform.javassist.JavassistTransformer
import com.didiglobal.booster.transform.util.TransformerClassLoader

class BoosterTestRunnerWithJavassist(
        clazz: Class<*>
) : BoosterTestRunner(clazz, TransformerClassLoader(
        clazz.classLoader,
        ClassTransformer::class.java::isAssignableFrom
) {
    JavassistTransformer(it)
})