package com.didiglobal.booster.aapt2

import java.io.Closeable
import java.io.File
import java.io.InputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.channels.FileChannel
import java.nio.file.StandardOpenOption

/**
 * Represents a binary data parser
 *
 * @author johnsonlee
 */
class BinaryParser : Closeable {

    private val _buffer: ByteBuffer

    private val _channel: FileChannel?

    private val _file: String

    /**
     * Initialize with the specified buffer
     *
     * @param buffer the buffer to parse
     */
    constructor(buffer: ByteBuffer) {
        _buffer = buffer
        _channel = null
        _file = "<buffer>"
    }

    /**
     * Initialize with the specified buffer
     *
     * @param buffer the buffer to parse
     */
    constructor(buffer: ByteArray) : this(ByteBuffer.wrap(buffer))

    /**
     * Initialize with the specified file and byte order
     *
     * @param file the file to parse
     * @param order the byte order, default is *little endian*
     */
    constructor(file: File, order: ByteOrder = ByteOrder.LITTLE_ENDIAN) {
        _channel = FileChannel.open(file.toPath(), StandardOpenOption.READ)
        _buffer = _channel.map(FileChannel.MapMode.READ_ONLY, 0, file.length()).order(order)
        _file = file.canonicalPath
    }

    /**
     * Initialize with the specified input stream and byte order
     *
     * @param input the input stream to parse
     * @param order the byte order, default is *little endian*
     */
    constructor(input: InputStream, order: ByteOrder = ByteOrder.LITTLE_ENDIAN) : this(ByteBuffer.wrap(input.readBytes()).order(order))

    /**
     * The internal buffer
     */
    internal val buffer: ByteBuffer
        get() = _buffer

    internal val file: String
        get() = _file

    /**
     * The capacity of buffer
     */
    val capacity: Int
        get() = _buffer.capacity()

    /**
     * The remaining size of buffer
     */
    val remaining: Int
        get() = _buffer.remaining()

    /**
     * Tells whether there are any elements between the current position and the limit.
     */
    val hasRemaining: Boolean
        get() = _buffer.hasRemaining()

    /**
     * Parses the remaining data in buffer as the specified type
     *
     * @param handler the handler for remaining data parsing
     */
    fun <T> parse(handler: (ByteBuffer) -> T) = handler(_buffer)

    /**
     * Returns the next byte
     */
    fun readByte() = _buffer.get()

    /**
     * Returns a byte value at the specified index
     */
    fun getByte(index: Int) = _buffer.get(index)

    /**
     * Returns the next short
     */
    fun readShort() = _buffer.short

    /**
     * Returns a short value at the specified index
     */
    fun getShort(index: Int) = _buffer.getShort(index)

    fun readChar() = _buffer.char

    fun getChar(index: Int) = _buffer.getChar(index)

    fun readInt() = _buffer.int

    fun getInt(index: Int) = _buffer.getInt(index)

    fun readLong() = _buffer.long

    fun getLong(index: Int) = _buffer.getLong(index)

    fun readBytes(size: Int) = ByteArray(size).also {
        _buffer.get(it)
    }

    fun getBytes(size: Int, index: Int): ByteArray {
        val p = tell()

        try {
            seek(index)
            return readBytes(size)
        } finally {
            seek(p)
        }
    }

    fun readUByte() = _buffer.get().toInt() and 0xff

    fun readUShort() = _buffer.short.toInt() and 0xffff

    fun readUInt() = _buffer.int

    fun getUByte(index: Int) = _buffer.get(index).toInt() and 0xff

    fun getUShort(index: Int) = _buffer.getShort(index).toInt() and 0xffff

    fun getUInt(index: Int) = _buffer.getInt(index)

    fun readUleb128(): Int {
        var result = 0
        var count = 0
        var cur: Int

        do {
            cur = _buffer.get().toInt() and 0xff
            result = result or (cur and 0x7f shl count * 7)
            count++
        } while (cur and 0x80 == 0x80 && count < 5)

        if (cur and 0x80 == 0x80) {
            throw RuntimeException("Invalid LEB128 sequence")
        }

        return result
    }

    fun readSleb128(): Int {
        var result = 0
        var cur: Int
        var count = 0
        var signBits = -1

        do {
            cur = readByte().toInt() and 0xff
            result = result or (cur and 0x7f shl count * 7)
            signBits = signBits shl 7
            count++
        } while (cur and 0x80 == 0x80 && count < 5)

        if (cur and 0x80 == 0x80) {
            throw RuntimeException("Invalid LEB128 sequence")
        }

        // Sign extend if appropriate
        if (signBits shr 1 and result != 0) {
            result = result or signBits
        }

        return result
    }

    fun skip(n: Int) = _buffer.position(_buffer.position() + n)

    fun seek(pos: Int) = _buffer.position(pos)

    fun tell() = _buffer.position()

    override fun close() {
        _channel?.close()
    }

}
