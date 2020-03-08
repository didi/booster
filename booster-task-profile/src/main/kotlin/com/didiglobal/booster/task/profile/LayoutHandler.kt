package com.didiglobal.booster.task.profile

import org.xml.sax.Attributes
import org.xml.sax.helpers.DefaultHandler

/**
 * @author johnsonlee
 */
class LayoutHandler : DefaultHandler() {

    private val _views = mutableSetOf<String>()

    val views: Set<String>
        get() = setOf(*_views.toTypedArray())

    override fun startElement(uri: String, localName: String, qName: String, attributes: Attributes) {
        _views += qName
    }


}