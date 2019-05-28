package com.didiglobal.booster.transform.asm

import org.objectweb.asm.Opcodes
import org.objectweb.asm.tree.AbstractInsnNode
import org.objectweb.asm.tree.ClassNode
import org.objectweb.asm.tree.MethodInsnNode
import org.objectweb.asm.tree.MethodNode
import org.objectweb.asm.tree.TypeInsnNode

/**
 * Replace `new Type()` with `ShadowType.newType()`
 *
 * @author johnsonlee
 */
fun TypeInsnNode.transform(klass: ClassNode, method: MethodNode, instantializer: TypeInsnNode, type: String, prefix: String = "") {
    var next: AbstractInsnNode? = this.next

    loop@ while (null != next) {
        if (Opcodes.INVOKESPECIAL != next.opcode) {
            next = next.next
            continue
        }

        val invoke = next as MethodInsnNode
        if (this.desc == invoke.owner && "<init>" == invoke.name) {
            // replace NEW with INVOKESTATIC
            invoke.owner = type
            invoke.name = "new$prefix${instantializer.desc.substring(instantializer.desc.lastIndexOf('/') + 1)}"
            invoke.desc = "${invoke.desc.substring(0, invoke.desc.lastIndexOf(')'))})L${instantializer.desc};"
            invoke.opcode = Opcodes.INVOKESTATIC
            invoke.itf = false

            // remove the next DUP of NEW
            val dup = instantializer.next
            if (Opcodes.DUP == dup.opcode) {
                method.instructions.remove(dup)
            } else {
                TODO("Unexpected instruction ${dup.opcode}: ${klass.name}.${method.name}${method.desc}")
            }
            method.instructions.remove(instantializer)
            break@loop
        }

        next = next.next
    }
}
