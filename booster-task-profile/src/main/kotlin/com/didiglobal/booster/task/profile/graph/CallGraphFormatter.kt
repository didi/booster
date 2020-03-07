package com.didiglobal.booster.task.profile.graph

/**
 * Represents a call graph formatter
 *
 * @author johnsonlee
 */
interface CallGraphFormatter {

    fun format(graph: CallGraph): CharSequence

}
