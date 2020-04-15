package com.didiglobal.booster.cha.graph

/**
 * Represents a call graph renderer
 *
 * @author johnsonlee
 */
interface CallGraphRenderer {

    fun render(graph: CallGraph): CharSequence

}
