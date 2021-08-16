package com.didiglobal.booster.task.resource.deredundancy

import com.android.SdkConstants
import com.android.build.gradle.api.BaseVariant
import com.didiglobal.booster.compression.CompressionResults
import com.didiglobal.booster.gradle.mergedManifests
import com.didiglobal.booster.kotlinx.search
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction
import javax.xml.parsers.SAXParserFactory

/**
 * Represents a task for redundant resources reducing
 */
internal open class RemoveRedundantImages : DefaultTask() {

    @get:Internal
    lateinit var variant: BaseVariant

    @get:Internal
    lateinit var results: CompressionResults

    @TaskAction
    open fun run() {
        TODO("Reducing redundant resources without aapt2 enabled has not supported yet")
    }

}

internal val BaseVariant.isSupportsRtl: Boolean
    get() {
        val parser = SAXParserFactory.newInstance().newSAXParser()
        return mergedManifests.search {
            it.name == SdkConstants.ANDROID_MANIFEST_XML
        }.parallelStream().map { manifest ->
            LayoutDirHandler().let {
                parser.parse(manifest, it)
                it.supportsRtl
            }
        }.toArray<Boolean> { size ->
            arrayOfNulls(size)
        }.fold(true) { acc, i ->
            acc and i
        }
    }