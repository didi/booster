package com.didiglobal.booster.kotlinx

import java.awt.Color.HSBtoRGB
import java.awt.Color.RGBtoHSB

class RGB(val r: Int = 0, val g: Int = 0, val b: Int = 0) {

    companion object {
        fun valueOf(rgb: Int) = RGB(rgb and 0xff, rgb shr 8 and 0xff, rgb shr 16 and 0xff)
    }

    fun light(percentage: Float): RGB {
        assert(percentage in 0.0..1.0)
        var (h, s, b) = RGBtoHSB(r, g, b, FloatArray(3))
        b *= percentage
        return RGB.valueOf(HSBtoRGB(h, s, b))
    }

    fun reverse() = RGB(255 - (r and 0xff), 255 - (g and 0xff), 255 - (b and 0xff))

    override fun toString() = String.format("%06X", (r and 0xff shl 16) or (g and 0xff shl 8) or (b and 0xff))
}
