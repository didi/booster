package com.didiglobal.booster.transform.shrink

import java.io.File
import java.util.Objects

/**
 * Represents the symbol list of resources
 *
 * @author johnsonlee
 */
class SymbolList private constructor(builder: Builder) : Iterable<SymbolList.Symbol<*>> {

    private val symbols = builder.symbols

    override fun iterator(): Iterator<Symbol<*>> = this.symbols.iterator()

    fun getInt(type: String, name: String): Int {
        return (this.symbols.find { it.type == type && it.name == name }?.value as? Int)!!
    }

    fun getIntArray(type: String = "styleable", name: String): IntArray {
        return (this.symbols.find { it.type == type && it.name == name && it is IntArraySymbol }?.value as? IntArray)!!
    }

    abstract class Symbol<out T>(val type: String, val name: String, val value: T) {

        override fun hashCode(): Int {
            return Objects.hash(type, name, value)
        }

        override fun equals(other: Any?): Boolean {
            if (this === other) {
                return true
            }

            if (other !is Symbol<*>) {
                return false
            }

            return type == other.type && name == other.name && value == other.value
        }
    }

    class IntSymbol(type: String, name: String, value: Int) : Symbol<Int>(type, name, value) {
        override fun toString(): String = "int $type $name $value"
    }

    class IntArraySymbol(type: String, name: String, value: IntArray) : Symbol<IntArray>(type, name, value) {
        override fun toString(): String = "int[] $type $name ${value.joinToString(", ", "{ ", " }") { "0x${it.toString(16)}" }}"
    }

    class Builder {

        val symbols = mutableListOf<Symbol<*>>()

        fun build(): SymbolList {
            return SymbolList(this)
        }

        fun addSymbol(symbol: Symbol<*>): Builder {
            this.symbols.add(symbol)
            return this
        }

    }

    companion object {

        /**
         * Parses symbols from the specified file
         *
         * @param file symbol list file
         */
        fun from(file: File) = SymbolList.Builder().also { builder ->
            if (file.exists()) {
                file.forEachLine { line ->
                    val sp1 = line.nextColumnIndex(' ')
                    val dataType = line.substring(0, sp1)
                    when (dataType) {
                        "int" -> {
                            val sp2 = line.nextColumnIndex(' ', sp1 + 1)
                            val type = line.substring(sp1 + 1, sp2)
                            val sp3 = line.nextColumnIndex(' ', sp2 + 1)
                            val name = line.substring(sp2 + 1, sp3)
                            val value: Int = line.substring(sp3 + 1).toInt()
                            builder.addSymbol(IntSymbol(type, name, value))
                        }
                        "int[]" -> {
                            val sp2 = line.nextColumnIndex(' ', sp1 + 1)
                            val type = line.substring(sp1 + 1, sp2)
                            val sp3 = line.nextColumnIndex(' ', sp2 + 1)
                            val name = line.substring(sp2 + 1, sp3)
                            val leftBrace = line.nextColumnIndex('{', sp3)
                            val rightBrace = line.prevColumnIndex('}')
                            val vStart = line.skipWhitespaceForward(leftBrace + 1)
                            val vEnd = line.skipWhitespaceBackward(rightBrace - 1) + 1
                            val values = mutableListOf<Int>()
                            var i = vStart

                            while (i < vEnd) {
                                val comma = line.nextColumnIndex(',', i, true)
                                i = if (comma > -1) {
                                    values.add(line.substring(line.skipWhitespaceForward(i), comma).toInt())
                                    line.skipWhitespaceForward(comma + 1)
                                } else {
                                    values.add(line.substring(i, vEnd).toInt())
                                    vEnd
                                }
                            }

                            builder.addSymbol(IntArraySymbol(type, name, values.toIntArray()))
                        }
                        else -> throw MalformedSymbolListException(file.absolutePath)
                    }
                }
            }
        }.build()

    }

}


private fun String.toInt(): Int {
    return if (startsWith("0x")) {
        substring(2).toInt(16)
    } else {
        toInt(10)
    }
}

private fun String.nextColumnIndex(delimiter: Char = ' ', startIndex: Int = 0, optional: Boolean = false): Int {
    val index = this.indexOf(delimiter, startIndex)
    if (!optional && index < 0) {
        throw MalformedSymbolListException(this)
    }
    return index
}

private fun String.prevColumnIndex(delimiter: Char = ' ', startIndex: Int = lastIndex, optional: Boolean = false): Int {
    val index = this.lastIndexOf(delimiter, startIndex)
    if (!optional && index < 0) {
        throw MalformedSymbolListException(this)
    }
    return index
}

private fun String.skipWhitespaceForward(startIndex: Int = 0): Int {
    var i = startIndex
    val n = length

    while (i < n) {
        if (!this[i].isWhitespace()) {
            break
        }
        i++
    }

    return i
}

private fun String.skipWhitespaceBackward(startIndex: Int = lastIndex): Int {
    var i = startIndex

    while (i >= 0) {
        if (!this[i].isWhitespace()) {
            break
        }
        i--
    }

    return i
}

