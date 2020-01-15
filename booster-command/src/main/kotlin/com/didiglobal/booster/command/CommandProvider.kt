package com.didiglobal.booster.command

/**
 * Represents a command provider
 *
 * @author johnsonlee
 */
interface CommandProvider {

    /**
     * returns a mapping of command name and url
     */
    fun get(): Collection<Command>

    /**
     * returns a command with the specific name
     *
     * @param name The command name
     */
    operator fun get(name: String): Command? = get().find { it.name == name }

}