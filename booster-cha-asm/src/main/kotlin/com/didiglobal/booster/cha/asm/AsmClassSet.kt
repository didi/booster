package com.didiglobal.booster.cha.asm

import com.didiglobal.booster.cha.ClassSet
import org.objectweb.asm.tree.ClassNode
import java.io.File

typealias AsmClassSet = ClassSet<ClassNode, AsmClassFileParser>

fun ClassSet.Companion.from(file: File) = from(file, AsmClassFileParser)