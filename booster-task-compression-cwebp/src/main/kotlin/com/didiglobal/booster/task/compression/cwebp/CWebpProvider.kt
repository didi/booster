package com.didiglobal.booster.task.compression.cwebp

import com.didiglobal.booster.command.Command
import com.didiglobal.booster.command.CommandProvider
import com.google.auto.service.AutoService

/**
 * Represents cwebp command provider
 *
 * @author johnsonlee
 */
@AutoService(CommandProvider::class)
class CWebpProvider : CommandProvider {

    override fun get(): Collection<Command> = listOf(Command(CWEBP, javaClass.classLoader.getResource(PREBUILT_CWEBP_EXECUTABLE)!!))

}