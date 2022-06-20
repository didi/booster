package com.didiglobal.booster.cha.asm

import org.objectweb.asm.tree.ClassNode
import java.time.Duration

typealias ProgressListener = (ClassNode, Float, Duration) -> Unit
