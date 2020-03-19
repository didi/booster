package com.didiglobal.booster.task.analyser.graph

/**
 * Represents a call graph renderer
 *
 * @author johnsonlee
 */
interface CallGraphRenderer {

    fun render(graph: CallGraph): CharSequence

}
