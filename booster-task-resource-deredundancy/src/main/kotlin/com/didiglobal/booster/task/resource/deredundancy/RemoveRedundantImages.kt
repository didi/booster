package com.didiglobal.booster.task.resource.deredundancy

import com.android.SdkConstants
import com.android.build.gradle.api.BaseVariant
import com.didiglobal.booster.compression.CompressionResults
import com.didiglobal.booster.gradle.mergedManifests
import com.didiglobal.booster.kotlinx.search
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import java.io.File
import javax.xml.parsers.SAXParserFactory

/**
 * Represents a task for redundant resources reducing
 */
internal open class RemoveRedundantImages: DefaultTask() {

    lateinit var variant: BaseVariant

    lateinit var results: CompressionResults

    lateinit var supplier: () -> Collection<File>

    val supportsRtl: Boolean
        get() {
            val parser = SAXParserFactory.newInstance().newSAXParser()
            return variant.mergedManifests.search {
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

    @TaskAction
    open fun run() {
        TODO("Reducing redundant resources without aapt2 enabled has not supported yet")
    }

}