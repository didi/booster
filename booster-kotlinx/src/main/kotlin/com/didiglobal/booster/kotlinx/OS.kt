package com.didiglobal.booster.kotlinx

import java.util.Locale

object OS {

    val name: String = System.getProperty("os.name", "").toLowerCase(Locale.US)

    val arch: String = System.getProperty("os.arch", "").toLowerCase(Locale.US)

    val version = object : Comparable<String> {

        private val version = System.getProperty("os.version", "").toLowerCase(Locale.US)

        override fun compareTo(other: String): Int {
            val part1 = version.split("[\\._\\-]".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            val part2 = other.split("[\\._\\-]".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()

            var idx = 0
            while (idx < part1.size && idx < part2.size) {
                val p1 = part1[idx]
                val p2 = part2[idx]
                val cmp = if (p1.matches("\\d+".toRegex()) && p2.matches("\\d+".toRegex())) {
                    p1.toInt().compareTo(p2.toInt())
                } else {
                    part1[idx].compareTo(part2[idx])
                }

                if (cmp != 0) {
                    return cmp
                }
                ++idx
            }

            if (part1.size == part2.size) {
                return 0
            }

            val left = part1.size > idx
            val parts = if (left) part1 else part2
            while (idx < parts.size) {
                val p = parts[idx]
                val cmp = if (p.matches("\\d+".toRegex())) {
                    p.toInt().compareTo(0)
                } else {
                    1
                }

                if (cmp != 0) {
                    return if (left) cmp else -cmp
                }
                ++idx
            }

            return 0
        }

        override fun toString() = this.version
    }

    val executableSuffix = if (isWindows()) ".exe" else ""

    fun isLinux() = name.startsWith("linux", true)

    fun isMac() = name.startsWith("mac")

    fun isWindows() = name.startsWith("windows")

}

