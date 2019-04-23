package com.didiglobal.booster.transform.lint

import java.util.Objects

/**
 * Represents the entry point of main thread / UI thread
 *
 * @author johnsonlee
 */
internal class EntryPoint(val name: String, val desc: String) {

    override fun hashCode(): Int {
        return Objects.hash(name, desc)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) {
            return true
        }
        if (other !is EntryPoint) {
            return false
        }

        return name == other.name && desc == other.desc
    }

    override fun toString(): String {
        return "$name$desc"
    }
}

internal val APPLICATION_ENTRY_POINTS = setOf(EntryPoint("onCreate", "()V"))

internal val ACTIVITY_ENTRY_POINTS = setOf(
        EntryPoint("onCreate", "(Landroid/os/Bundle;)V"),
        EntryPoint("onStart", "()V"),
        EntryPoint("onResume", "()V"),
        EntryPoint("onPause", "()V"),
        EntryPoint("onStop", "()V"),
        EntryPoint("onDestroy", "()")
)

internal val SERVICE_ENTRY_POINTS = setOf(
        EntryPoint("onCreate", "()V"),
        EntryPoint("onStartCommand", "(Landroid/content/Intent;II)I")
)
