package com.didiglobal.booster.transform.asm

import com.didiglobal.booster.annotations.Priority
import com.google.auto.service.AutoService

@Priority(Int.MIN_VALUE)
@AutoService(ClassTransformer::class)
class LowestPriorityTransformer : ClassTransformer {
}