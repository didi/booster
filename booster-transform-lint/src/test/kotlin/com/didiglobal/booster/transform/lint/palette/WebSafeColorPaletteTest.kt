package com.didiglobal.booster.transform.lint.palette

import kotlin.test.Test
import kotlin.test.assertNotEquals

class WebSafeColorPaletteTest {

    @Test
    fun `generate random color`() {
        assertNotEquals(0x000000, WEB_SAFE_COLOR_PALETTE.random(0x000000))
        assertNotEquals(0x0000ff, WEB_SAFE_COLOR_PALETTE.random(0x0000ff))
        assertNotEquals(0x00ff00, WEB_SAFE_COLOR_PALETTE.random(0x00ff00))
        assertNotEquals(0xff0000, WEB_SAFE_COLOR_PALETTE.random(0xff0000))
        assertNotEquals(0xffff00, WEB_SAFE_COLOR_PALETTE.random(0x00ffff))
        assertNotEquals(0xff00ff, WEB_SAFE_COLOR_PALETTE.random(0xff00ff))
        assertNotEquals(0x00ffff, WEB_SAFE_COLOR_PALETTE.random(0x00ffff))
        assertNotEquals(0xffffff, WEB_SAFE_COLOR_PALETTE.random(0xffffff))
    }

}
