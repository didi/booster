package com.didiglobal.booster.kotlinx

import java.util.ArrayList
import java.util.Stack

/**
 * Represents a wildcard
 *
 * @author johnsonlee
 */
class Wildcard(private val pattern: String, private val ignoreCase: Boolean = false) {

    fun matches(text: String): Boolean {
        val wcs = splitOnTokens(pattern)
        var anyChars = false
        var textIdx = 0
        var wcsIdx = 0
        val backtrack = Stack<IntArray>()

        do {
            if (backtrack.size > 0) {
                val array = backtrack.pop()
                wcsIdx = array[0]
                textIdx = array[1]
                anyChars = true
            }

            while (wcsIdx < wcs.size) {

                if (wcs[wcsIdx] == "?") {
                    textIdx++
                    if (textIdx > text.length) {
                        break
                    }
                    anyChars = false

                } else if (wcs[wcsIdx] == "*") {
                    anyChars = true
                    if (wcsIdx == wcs.size - 1) {
                        textIdx = text.length
                    }

                } else {
                    if (anyChars) {
                        textIdx = checkIndexOf(text, textIdx, wcs[wcsIdx])
                        if (textIdx == -1) {
                            break
                        }
                        val repeat = checkIndexOf(text, textIdx + 1, wcs[wcsIdx])
                        if (repeat >= 0) {
                            backtrack.push(intArrayOf(wcsIdx, repeat))
                        }
                    } else {
                        if (!checkRegionMatches(text, textIdx, wcs[wcsIdx])) {
                            break
                        }
                    }

                    textIdx += wcs[wcsIdx].length
                    anyChars = false
                }

                wcsIdx++
            }

            if (wcsIdx == wcs.size && textIdx == text.length) {
                return true
            }

        } while (backtrack.size > 0)

        return false
    }

    override fun hashCode() = this.pattern.hashCode()

    override fun equals(other: Any?) = when {
        other === this -> true
        other is Wildcard -> other.pattern == this.pattern
        else -> false
    }

    override fun toString() = this.pattern

    companion object {
        fun valueOf(pattern: String) = Wildcard(pattern)
    }

    private fun splitOnTokens(text: String): Array<String> {
        // used by wildcardMatch
        // package level so a unit test may run on this

        if (text.indexOf('?') == -1 && text.indexOf('*') == -1) {
            return arrayOf(text)
        }

        val array = text.toCharArray()
        val list = ArrayList<String>()
        val buffer = StringBuilder()
        for (i in array.indices) {
            if (array[i] == '?' || array[i] == '*') {
                if (buffer.isNotEmpty()) {
                    list.add(buffer.toString())
                    buffer.setLength(0)
                }
                if (array[i] == '?') {
                    list.add("?")
                } else if (list.isEmpty() || i > 0 && list[list.size - 1] != "*") {
                    list.add("*")
                }
            } else {
                buffer.append(array[i])
            }
        }
        if (buffer.isNotEmpty()) {
            list.add(buffer.toString())
        }

        return list.toTypedArray()
    }

    private fun checkIndexOf(str: String, strStartIndex: Int, search: String): Int {
        val endIndex = str.length - search.length
        if (endIndex >= strStartIndex) {
            for (i in strStartIndex..endIndex) {
                if (checkRegionMatches(str, i, search)) {
                    return i
                }
            }
        }
        return -1
    }

    private fun checkRegionMatches(str: String, strStartIndex: Int, search: String): Boolean {
        return str.regionMatches(strStartIndex, search, 0, search.length, ignoreCase = ignoreCase)
    }

}
