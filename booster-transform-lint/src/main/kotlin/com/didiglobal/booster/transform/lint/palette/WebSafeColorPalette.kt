package com.didiglobal.booster.transform.lint.palette

import java.util.Random

/**
 * [216 web safe colors](http://www.color-hex.com/216-web-safe-colors/)
 *
 * @author johnsonlee
 */
object WebSafeColorPalette {

    private const val N = 216

    private val colors = IntArray(N)

    private val scales = arrayOf(0, 0x33, 0x66, 0x99, 0xcc, 0xff)

    init {
        val n = scales.size - 1
        for (r in 0..n) {
            for (g in 0..n) {
                for (b in 0..n) {
                    colors[r * n * n + g * n + b] = (scales[r] shl 16) or (scales[g] shl 8) or scales[b]
                }
            }
        }
    }

    val matrix: IntArray
        get() = colors.copyOf()

    fun random(vararg except: Int = IntArray(0)): Int {
        val rand = Random()
        val rgbs = except.map { it and 0xffffff }.toIntArray()

        while (true) {
            val color = colors[rand.nextInt(N)]
            if (!rgbs.contains(color)) {
                return color
            }
        }
    }

}
