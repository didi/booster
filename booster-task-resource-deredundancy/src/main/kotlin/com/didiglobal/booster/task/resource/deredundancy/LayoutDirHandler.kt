package com.didiglobal.booster.task.resource.deredundancy

import org.xml.sax.Attributes
import org.xml.sax.helpers.DefaultHandler

internal class LayoutDirHandler : DefaultHandler() {

    var supportsRtl = false

    override fun startElement(uri: String, localName: String, qName: String, attributes: Attributes) {
        if ("application" == qName) {
            this.supportsRtl = attributes.getValue("android:supportsRtl")?.toBoolean() ?: false
        }
    }

}
