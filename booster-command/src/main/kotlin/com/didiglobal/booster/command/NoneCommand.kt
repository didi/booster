package com.didiglobal.booster.command

import java.io.ByteArrayInputStream
import java.io.File
import java.io.InputStream
import java.net.URL
import java.net.URLConnection
import java.net.URLStreamHandler

/**
 * Represents a command which doesn't installed
 *
 * @author johnsonlee
 */
internal class NoneCommand(name: String) : Command(name, URL("cmd", "localhost", 9102, "/${name}", HANDLER), File(System.getProperty("java.io.tmpdir"), name)) {

    override fun install(location: File) = false

}

private val HANDLER = object : URLStreamHandler() {

    override fun openConnection(url: URL?) = object : URLConnection(url) {
        override fun connect() {
        }

        override fun getInputStream(): InputStream = ByteArrayInputStream(ByteArray(0))
    }

}
