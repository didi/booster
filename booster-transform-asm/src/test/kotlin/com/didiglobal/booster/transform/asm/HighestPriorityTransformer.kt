package com.didiglobal.booster.transform.asm

import com.didiglobal.booster.annotations.Priority
import com.google.auto.service.AutoService

@Priority(Int.MAX_VALUE)
@AutoService(ClassTransformer::class)
class HighestPriorityTransformer : ClassTransformer {
}