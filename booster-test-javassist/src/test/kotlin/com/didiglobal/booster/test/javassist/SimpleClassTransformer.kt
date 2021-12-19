package com.didiglobal.booster.test.javassist

import com.didiglobal.booster.transform.TransformContext
import com.didiglobal.booster.transform.javassist.ClassTransformer
import com.google.auto.service.AutoService
import javassist.CtClass

@AutoService(ClassTransformer::class)
class SimpleClassTransformer : ClassTransformer {

    override fun transform(context: TransformContext, klass: CtClass): CtClass {
        println(klass.name)
        return klass
    }

}