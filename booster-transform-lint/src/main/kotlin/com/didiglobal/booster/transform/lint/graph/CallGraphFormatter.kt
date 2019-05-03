package com.didiglobal.booster.transform.lint.graph

/**
 * Represents a call graph formatter
 *
 * @author johnsonlee
 */
interface CallGraphFormatter {

    fun format(graph: CallGraph): CharSequence

}
