package com.didiglobal.booster.task.compression.cwebp

import org.gradle.api.tasks.CacheableTask
import java.io.File

/**
 * Represents a task for compiled image compression using cwebp
 *
 * @author johnsonlee
 */
@CacheableTask
internal open class CwebpCompressOpaqueFlatImages : CwebpCompressFlatImages() {

    override fun compress(filter: (File) -> Boolean) = super.compress { true }

}

