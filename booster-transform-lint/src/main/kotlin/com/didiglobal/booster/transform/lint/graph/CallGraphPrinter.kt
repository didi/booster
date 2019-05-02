package com.didiglobal.booster.transform.lint.graph

import java.io.BufferedWriter
import java.io.File
import java.io.OutputStream
import java.io.OutputStreamWriter
import java.io.PrintWriter
import java.io.Writer
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets

/**
 * Represents a call graph printer
 *
 * @author johnsonlee
 */
abstract class CallGraphPrinter(private val writer: Writer, private val autoFlush: Boolean = true) : PrintWriter(writer, autoFlush) {

    constructor(output: OutputStream, autoFlush: Boolean = true) : this(BufferedWriter(OutputStreamWriter(output)), autoFlush)

    constructor(output: File, charset: Charset = StandardCharsets.UTF_8, autoFlush: Boolean = true) : this(BufferedWriter(OutputStreamWriter(output.outputStream(), charset)), autoFlush)

    constructor(path: String, charset: Charset = StandardCharsets.UTF_8, autoFlush: Boolean = true) : this(File(path), charset, autoFlush)

    abstract fun print(graph: CallGraph)

}
