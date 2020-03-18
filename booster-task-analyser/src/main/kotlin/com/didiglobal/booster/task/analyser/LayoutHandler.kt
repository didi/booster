package com.didiglobal.booster.task.analyser

import org.xml.sax.Attributes
import org.xml.sax.helpers.DefaultHandler

/**
 * @author johnsonlee
 */
class LayoutHandler : DefaultHandler() {

    val views = mutableSetOf<String>()

    override fun startElement(uri: String, localName: String, qName: String, attributes: Attributes) {
        views += qName
    }

}