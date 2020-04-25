package com.didiglobal.booster.transform.util

import com.didiglobal.booster.transform.TransformContext
import com.didiglobal.booster.transform.asm.AsmTransformer
import com.didiglobal.booster.transform.asm.ClassTransformer
import org.objectweb.asm.tree.ClassNode
import java.io.File
import java.io.FileNotFoundException
import kotlin.test.Test

class TransformHelperTest {

    @Test
    fun `transform jars`() {
        try {
            val input = File(TransformHelperTest::class.java.classLoader.getResource("booster")!!.file)

            TransformHelper(input).transform(AsmTransformer(object : ClassTransformer {
                override fun transform(context: TransformContext, klass: ClassNode): ClassNode {
                    println(klass.name)
                    return klass
                }
            }))
        } catch (e: RuntimeException) {
            println(e.localizedMessage)
        } catch (e: FileNotFoundException) {
            println(e.localizedMessage)
        }
    }

}