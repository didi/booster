package com.didiglobal.booster.gradle

val GTE_V3_X: Boolean by lazy { AGP.revision.major >= 3 }
val GTE_V3_6: Boolean by lazy { AGP.revision.major > 3 || (AGP.revision.major == 3 && AGP.revision.minor >= 6) }
val GTE_V3_5: Boolean by lazy { AGP.revision.major > 3 || (AGP.revision.major == 3 && AGP.revision.minor >= 5) }
val GTE_V3_4: Boolean by lazy { AGP.revision.major > 3 || (AGP.revision.major == 3 && AGP.revision.minor >= 4) }
val GTE_V3_3: Boolean by lazy { AGP.revision.major > 3 || (AGP.revision.major == 3 && AGP.revision.minor >= 3) }
val GTE_V3_2: Boolean by lazy { AGP.revision.major > 3 || (AGP.revision.major == 3 && AGP.revision.minor >= 2) }
val GTE_V3_1: Boolean by lazy { AGP.revision.major > 3 || (AGP.revision.major == 3 && AGP.revision.minor >= 1) }

val GTE_V4_X: Boolean by lazy { AGP.revision.major >= 4 }
val GTE_V4_2: Boolean by lazy { AGP.revision.major > 4 || (AGP.revision.major == 4 && AGP.revision.minor >= 2) }
val GTE_V4_1: Boolean by lazy { AGP.revision.major > 4 || (AGP.revision.major == 4 && AGP.revision.minor >= 1) }

val GTE_V7_X: Boolean by lazy { AGP.revision.major >= 7 }
val GTE_V7_2: Boolean by lazy { AGP.revision.major > 7 || (AGP.revision.major == 7 && AGP.revision.minor >= 2) }
