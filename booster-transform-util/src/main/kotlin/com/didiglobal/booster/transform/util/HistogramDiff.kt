package com.didiglobal.booster.transform.util

import org.eclipse.jgit.diff.DiffAlgorithm
import org.eclipse.jgit.diff.DiffFormatter
import org.eclipse.jgit.diff.HistogramDiff
import org.eclipse.jgit.diff.RawText
import org.eclipse.jgit.diff.RawTextComparator
import java.io.ByteArrayOutputStream

infix fun String.diff(other: String): String {
    val a = RawText(this.toByteArray())
    val b = RawText(other.toByteArray())
    val diff = HistogramDiff().diff(RawTextComparator.DEFAULT, a, b)
    val patch = ByteArrayOutputStream()
    DiffFormatter(patch).apply {
        setDiffAlgorithm(DiffAlgorithm.getAlgorithm(DiffAlgorithm.SupportedAlgorithm.HISTOGRAM))
        setDiffComparator(RawTextComparator.DEFAULT)
    }.format(diff, a, b)
    return patch.toString()
}
