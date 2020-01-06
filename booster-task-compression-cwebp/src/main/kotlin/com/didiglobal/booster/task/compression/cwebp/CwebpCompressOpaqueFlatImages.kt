package com.didiglobal.booster.task.compression.cwebp

import org.gradle.api.tasks.TaskAction

/**
 * Represents a task for compiled image compression using cwebp
 *
 * @author johnsonlee
 */
internal open class CwebpCompressOpaqueFlatImages : CwebpCompressFlatImages() {

    @TaskAction
    override fun run() {
        compress { true }
    }

}

