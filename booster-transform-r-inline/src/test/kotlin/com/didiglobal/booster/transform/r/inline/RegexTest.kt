package com.didiglobal.booster.transform.r.inline

import java.io.File
import java.util.regex.Pattern
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class RegexTest {

    @Test
    fun `test regex`() {
        assertFalse {
            Pattern.matches(R_REGEX, "kotlin/reflect/KProperty0\$Getter")
        }

        assertTrue {
            Pattern.matches(R_REGEX, "androidx/loader/R")
        }

        assertTrue {
            Pattern.matches(R_REGEX, "io/github/boostersamples/R\$styleable")
        }
    }

}
