package com.didiglobal.booster.transform.util

import org.apache.bcel.Const
import org.apache.bcel.classfile.ClassFormatException
import org.apache.bcel.classfile.ConstantClass
import org.apache.bcel.classfile.ConstantPool
import java.io.DataInputStream
import java.io.InputStream

class ClassFileSnapshot(private val input: InputStream) {

    private var bytecode: DataInputStream = DataInputStream(input.buffered())

    constructor(bytecode: ByteArray) : this(bytecode.inputStream())

    /**
     * Minor version
     */
    val minor: Int

    /**
     * Major version
     */
    val major: Int

    /**
     * Imported classes
     */
    val imports: List<String>

    /**
     * Access flags
     */
    val accessFlags: Int

    /**
     * This class name
     */
    val name: String

    /**
     * Super class name
     */
    val superName: String

    /**
     * Interfaces
     */
    val interfaces: List<String>

    init {
        if (bytecode.readInt() != Const.JVM_CLASSFILE_MAGIC) {
            throw ClassFormatException()
        }

        this.minor = this.bytecode.readUnsignedShort()
        this.major = this.bytecode.readUnsignedShort()

        val pool = ConstantPool(bytecode)
        this.imports = pool.constantPool.mapIndexed { i, const ->
            i to const
        }.filter {
            it.second != null && it.second.tag == Const.CONSTANT_Class
        }.map {
            it.second as ConstantClass
            pool.getConstantString(it.first, it.second.tag)
        }
        this.accessFlags = bytecode.readUnsignedShort() // access_flags
        this.name = pool.getConstantString(bytecode.readUnsignedShort(), Const.CONSTANT_Class)
        this.superName = pool.getConstantString(bytecode.readUnsignedShort(), Const.CONSTANT_Class)
        this.interfaces = (0..bytecode.readUnsignedShort()).map {
            pool.getConstantString(bytecode.readUnsignedShort(), Const.CONSTANT_Class)
        }
    }

}
