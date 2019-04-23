package com.didiglobal.booster.util

import org.xml.sax.Attributes
import org.xml.sax.helpers.DefaultHandler

const val ATTR_NAME = "android:name"

class ComponentHandler : DefaultHandler() {

    val applications = mutableSetOf<String>()
    val activities = mutableSetOf<String>()
    val services = mutableSetOf<String>()
    val providers = mutableSetOf<String>()
    val receivers = mutableSetOf<String>()

    override fun startElement(uri: String, localName: String, qName: String, attributes: Attributes) {
        when (qName) {
            "application" -> {
                applications.add(attributes.getValue(ATTR_NAME))
            }
            "activity" -> {
                activities.add(attributes.getValue(ATTR_NAME))
            }
            "service" -> {
                services.add(attributes.getValue(ATTR_NAME))
            }
            "provider" -> {
                providers.add(attributes.getValue(ATTR_NAME))
            }
            "receiver" -> {
                receivers.add(attributes.getValue(ATTR_NAME))
            }
        }
    }

}
