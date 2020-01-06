package com.didiglobal.booster.task.compression.cwebp

import org.xml.sax.Attributes
import org.xml.sax.helpers.DefaultHandler

/**
 * Represents a handler for launcher icon parsing
 *
 * @author johnsonlee
 */
internal class LauncherIconHandler : DefaultHandler() {

    private val _icons = mutableSetOf<String>()

    val icons: Set<String>
        get() = _icons

    override fun startElement(uri: String, localName: String, qName: String, attributes: Attributes) {
        when (qName) {
            "application" -> {
                attributes.getValue("android:icon")?.let {
                    _icons.add(it.substringAfter('@'))
                }
                attributes.getValue("android:roundIcon")?.let {
                    _icons.add(it.substringAfter('@'))
                }
            }
        }
    }

}
