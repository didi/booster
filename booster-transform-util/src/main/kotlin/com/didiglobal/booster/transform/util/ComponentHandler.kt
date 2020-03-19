package com.didiglobal.booster.transform.util

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
        val name: String = attributes.getValue(ATTR_NAME) ?: return

        when (qName) {
            "application" -> {
                applications.add(name)
            }
            "activity" -> {
                activities.add(name)
            }
            "service" -> {
                services.add(name)
            }
            "provider" -> {
                providers.add(name)
            }
            "receiver" -> {
                receivers.add(name)
            }
        }
    }

}
