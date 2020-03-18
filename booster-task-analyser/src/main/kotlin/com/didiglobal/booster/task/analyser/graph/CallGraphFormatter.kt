package com.didiglobal.booster.task.analyser.graph

/**
 * Represents a call graph formatter
 *
 * @author johnsonlee
 */
interface CallGraphFormatter {

    fun format(graph: CallGraph): CharSequence

}
