package com.didiglobal.booster.transform.asm

import com.didiglobal.booster.kotlinx.asIterable
import org.objectweb.asm.tree.AbstractInsnNode
import org.objectweb.asm.tree.InsnList

fun InsnList.asIterable() = this.iterator().asIterable()

/**
 * Find the first instruction node with the specified opcode
 *
 * @param opcode The opcode to search
 */
fun InsnList.find(opcode: Int) = this.asIterable().find { it.opcode == opcode }

/**
 * Find all of instruction nodes with the specified opcode
 *
 * @param opcodes The opcode to search
 */
fun InsnList.findAll(vararg opcodes: Int) = this.filter { it.opcode in opcodes }

fun InsnList.filter(predicate: (AbstractInsnNode) -> Boolean) = this.asIterable().filter(predicate)

fun InsnList.any(predicate: (AbstractInsnNode) -> Boolean) = this.asIterable().any(predicate)
