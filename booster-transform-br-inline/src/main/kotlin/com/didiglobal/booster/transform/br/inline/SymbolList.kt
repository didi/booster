package com.didiglobal.booster.transform.br.inline

import org.objectweb.asm.ClassReader
import org.objectweb.asm.tree.ClassNode
import java.io.File

/**
 * Represents the symbol list of BR
 *
 * @author linjiang
 */
class SymbolList private constructor(builder: Builder) : Iterable<SymbolList.Symbol> {

    private val symbols = builder.symbols

    override fun iterator(): Iterator<Symbol> = this.symbols.iterator()

    fun getInt(name: String): Int {
        return this.symbols.find { it.name == name }?.value!!
    }

    fun isEmpty() = this.symbols.isEmpty()

    data class Symbol(val name: String, val value: Int)


    class Builder {

        val symbols = mutableListOf<Symbol>()

        fun build(): SymbolList {
            return SymbolList(this)
        }

        fun addSymbol(symbol: Symbol): Builder {
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
        fun from(file: File) = Builder().also { builder ->
            if (file.exists()) {
                ClassNode().also {
                    ClassReader(file.inputStream()).accept(it, 0)
                }.fields.forEach {
                    if (it.value is Int) {
                        builder.addSymbol(Symbol(it.name, it.value as Int))
                    }
                }
            }
        }.build()

    }

}
