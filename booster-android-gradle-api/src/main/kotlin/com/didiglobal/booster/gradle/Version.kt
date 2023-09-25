package com.didiglobal.booster.gradle

val GTE_V8_X: Boolean by lazy { AGP.revision.major >= 8 }

val GTE_V8_1: Boolean by lazy { AGP.revision.major > 8 || (AGP.revision.major == 8 && AGP.revision.minor >= 1) }

val GTE_V8_2: Boolean by lazy { AGP.revision.major > 8 || (AGP.revision.major == 8 && AGP.revision.minor >= 2) }
