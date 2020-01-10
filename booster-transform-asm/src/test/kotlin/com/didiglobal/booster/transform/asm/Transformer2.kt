package com.didiglobal.booster.transform.asm

import com.didiglobal.booster.annotations.Priority
import com.google.auto.service.AutoService

@Priority(2)
@AutoService(ClassTransformer::class)
class Transformer2 : ClassTransformer {
}