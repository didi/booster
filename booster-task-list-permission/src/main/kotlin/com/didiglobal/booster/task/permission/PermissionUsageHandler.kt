package com.didiglobal.booster.task.permission

import org.xml.sax.Attributes
import org.xml.sax.helpers.DefaultHandler

internal class PermissionUsageHandler : DefaultHandler() {

    internal val permissions = mutableSetOf<String>()

    override fun startElement(uri: String, localName: String, qName: String, attributes: Attributes) {
        when (localName) {
            "uses-permission" -> permissions.add(attributes.getValue("android:name"))
            "uses-permission-sdk-23" -> permissions.add(attributes.getValue("android:name"))
            "uses-permission-sdk-m" -> permissions.add(attributes.getValue("android:name"))
        }
    }

}
