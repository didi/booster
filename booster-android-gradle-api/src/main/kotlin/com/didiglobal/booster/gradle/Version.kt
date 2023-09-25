package com.didiglobal.booster.gradle

val GTE_V8_X: Boolean by lazy { AGP.revision.major >= 8 }

val GTE_V8_1: Boolean by lazy { AGP.revision.major > 8 || (AGP.revision.major == 1 && AGP.revision.minor >= 0) }