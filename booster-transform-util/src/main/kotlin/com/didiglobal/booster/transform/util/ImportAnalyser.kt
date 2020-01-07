package com.didiglobal.booster.transform.util

import org.apache.bcel.Const
import org.apache.bcel.classfile.ClassFormatException
import org.apache.bcel.classfile.ConstantClass
import org.apache.bcel.classfile.ConstantPool
import java.io.DataInputStream
import java.io.InputStream

class ImportAnalyser(private val input: InputStream) {

    private var bytecode: DataInputStream = DataInputStream(input.buffered())

    constructor(bytecode: ByteArray) : this(bytecode.inputStream())

    fun analyse(): Collection<String> {
        val magic = bytecode.readInt()
        if (magic != Const.JVM_CLASSFILE_MAGIC) {
            throw ClassFormatException()
        }

        bytecode.readUnsignedShort()
        bytecode.readUnsignedShort()

        return ConstantPool(bytecode).let { pool ->
            pool.constantPool.mapIndexed { i, const ->
                i to const
            }.filter {
                it.second != null && it.second.tag == Const.CONSTANT_Class
            }.map {
                it.second as ConstantClass
                pool.getConstantString(it.first, it.second.tag)
            }
        }
    }

}
