package com.didiglobal.booster.transform.util

import kotlin.test.Test
import kotlin.test.assertEquals

class ArgumentsParserTest {

    @Test
    fun `parse arg list`() {
        assertEquals("(boolean, byte, char, short, int, float, long, double, int[])", ArgumentsParser("ZBCSIFJD[I").parse().joinToString(", ", "(", ")"))
        assertEquals("(boolean, byte, char, short, int, float, long, double, int[])", ArgumentsParser("abcdefg(ZBCSIFJD[I)Ljava/lang/String;", 8, 10).parse().joinToString(", ", "(", ")"))
        assertEquals("(boolean, byte, char, short, int, float, long, double, int[], java.lang.String)", ArgumentsParser("abcdefg(ZBCSIFJD[ILjava/lang/String;)Ljava/lang/String;", 8, 28).parse().joinToString(", ", "(", ")"))
        assertEquals("(boolean, byte, char, short, int, float, long, double, int[], java.lang.String[])", ArgumentsParser("abcdefg(ZBCSIFJD[I[Ljava/lang/String;)Ljava/lang/String;", 8, 29).parse().joinToString(", ", "(", ")"))
    }

}
