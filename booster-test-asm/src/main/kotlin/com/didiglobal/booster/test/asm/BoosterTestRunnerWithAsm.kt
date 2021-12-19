package com.didiglobal.booster.test.asm

import com.didiglobal.booster.test.BoosterTestRunner
import com.didiglobal.booster.transform.asm.AsmTransformer
import com.didiglobal.booster.transform.asm.ClassTransformer
import com.didiglobal.booster.transform.util.TransformerClassLoader

class BoosterTestRunnerWithAsm(
        clazz: Class<*>
) : BoosterTestRunner(clazz, TransformerClassLoader(
        clazz.classLoader,
        ClassTransformer::class.java::isAssignableFrom
) {
    AsmTransformer(it)
})
