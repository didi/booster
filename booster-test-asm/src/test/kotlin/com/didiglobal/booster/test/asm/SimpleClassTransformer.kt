package com.didiglobal.booster.test.asm

import com.didiglobal.booster.transform.Collector
import com.didiglobal.booster.transform.TransformContext
import com.didiglobal.booster.transform.asm.ClassTransformer
import com.google.auto.service.AutoService
import org.objectweb.asm.tree.ClassNode

@AutoService(ClassTransformer::class)
class SimpleClassTransformer : ClassTransformer {

    override fun onPreTransform(context: TransformContext) {
        context.registerCollector(object : Collector<String?> {
            override fun accept(name: String): Boolean = name == ""
            override fun collect(name: String, data: () -> ByteArray): String? = name
        })
    }

    override fun transform(context: TransformContext, klass: ClassNode): ClassNode {
        println(klass.name)
        return klass
    }

}