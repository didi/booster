@file:Suppress("DEPRECATION")

package com.didiglobal.booster.android.gradle.v8_1

import com.android.build.api.variant.AndroidComponentsExtension
import com.android.build.api.variant.Variant
import com.didiglobal.booster.gradle.AGP
import com.didiglobal.booster.gradle.assembleTaskProvider
import com.didiglobal.booster.gradle.getAndroidComponents
import com.didiglobal.booster.gradle.project
import com.didiglobal.booster.kotlinx.capitalized
import com.didiglobal.booster.kotlinx.search
import io.bootstage.testkit.gradle.Case
import io.bootstage.testkit.gradle.TestCase
import io.bootstage.testkit.gradle.VariantTestCase
import io.bootstage.testkit.gradle.rules.GradleExecutor
import io.bootstage.testkit.gradle.rules.LocalProperties
import io.bootstage.testkit.gradle.rules.copyFromResource
import io.bootstage.testkit.gradle.rules.rule
import org.gradle.api.Project
import org.junit.Before
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import org.junit.rules.TestRule
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import kotlin.test.fail

private val MIN_SDK_VERSION = System.getProperty("android.minsdk.version").toInt()

private const val TARGET_SDK_VERSION = 30

private val ARGS = System.getProperty("gradle.args").split("\\s+".toRegex()) + listOf(
    "-Pbooster_version=${Build.VERSION}",
    "-Pandroid_gradle_version=8.1.0",
    "-Pcompile_sdk_version=30",
    "-Pbuild_tools_version=29.0.2",
    "-Pmin_sdk_version=$MIN_SDK_VERSION",
    "-Ptarget_sdk_version=$TARGET_SDK_VERSION"
)

@Suppress("RemoveCurlyBracesFromTemplate", "FunctionName")
abstract class V81IntegrationTest(private val isLib: Boolean) {

    private val projectDir = TemporaryFolder()

    @get:Rule
    val ruleChain: TestRule = rule(projectDir) {
        rule(LocalProperties(projectDir::getRoot)) {
            GradleExecutor(projectDir::getRoot, "8.0", *ARGS.toTypedArray())
        }
    }

    @Before
    fun setup() {
        projectDir.copyFromResource("${if (isLib) "lib" else "app"}.gradle", "build.gradle")
        projectDir.copyFromResource("buildSrc")
        projectDir.copyFromResource("src")
        projectDir.newFile("gradle.properties").writeText("org.gradle.jvmargs=-Xmx4g -XX:MaxMetaspaceSize=2g")
        assertEquals(8, AGP.revision.major)
        assertEquals(1, AGP.revision.minor)
    }


    @Test
    @Case(ProjectTest::class)
    fun `test AGPInterface#project`() = Unit

    @Test
    @Case(JavaCompilerTaskTestUnit::class)
    fun `test AGPInterface#javaCompilerTask`() = Unit

    @Test
    @Case(PreBuildTaskTestUnit::class)
    fun `test AGPInterface#preBuildTask`() = Unit

    @Test
    @Case(AssembleTaskTestUnit::class)
    fun `test AGPInterface#assembleTask`() = Unit

    @Test
    @Case(MergeAssetsTaskTestUnit::class)
    fun `test AGPInterface#mergeAssetsTask`() = Unit

    @Test
    @Case(MergeResourcesTaskTestUnit::class)
    fun `test AGPInterface#mergeResources`() = Unit

    @Test
    @Case(AllClassesTestUnit::class)
    fun `test AGPInterface#allClasses`() {
    }

}

class V81AppIntegrationTest : V81IntegrationTest(false)

class V81LibIntegrationTest : V81IntegrationTest(true)


class ProjectTest : TestCase {
    override fun apply(project: Project) {
        val assert: (Variant) -> Unit = { variant ->
            assertEquals(project, AGP.run { variant.project })
        }
        project.getAndroidComponents<AndroidComponentsExtension<*, *, *>>().onVariants(callback = assert)
    }
}

class JavaCompilerTaskTestUnit : VariantTestCase() {
    override fun apply(variant: Variant) {
        variant.project.gradle.taskGraph.whenReady {
            assertNotNull(AGP.run {
                variant.javaCompilerTask
            })
        }
    }
}

class PreBuildTaskTestUnit : VariantTestCase() {
    override fun apply(variant: Variant) {
        variant.project.gradle.taskGraph.whenReady {
            assertNotNull(AGP.run { variant.preBuildTask })
        }
    }
}

class AssembleTaskTestUnit : VariantTestCase() {
    override fun apply(variant: Variant) {
        variant.project.gradle.taskGraph.whenReady {
            assertNotNull(AGP.run { variant.assembleTask })
        }
    }
}

class MergeAssetsTaskTestUnit : VariantTestCase() {
    override fun apply(variant: Variant) {
        variant.project.gradle.taskGraph.whenReady {
            assertNotNull(AGP.run { variant.mergeAssetsTask })
        }
    }
}

class MergeResourcesTaskTestUnit : VariantTestCase() {
    override fun apply(variant: Variant) {
        variant.project.gradle.taskGraph.whenReady {
            assertNotNull(AGP.run { variant.mergeResourcesTask })
        }
    }
}

class GetTaskNameTestUnit : VariantTestCase() {
    override fun apply(variant: Variant) {
        variant.project.gradle.taskGraph.whenReady {
            assertEquals("assemble${variant.name.capitalized()}", AGP.run { variant.getTaskName("assemble") })
        }
    }
}


class GetTaskName2TestUnit : VariantTestCase() {
    override fun apply(variant: Variant) {
        variant.project.gradle.taskGraph.whenReady {
            assertEquals("merge${variant.name.capitalized()}Resources", AGP.run { variant.getTaskName("merge", "Resources") })
        }
    }
}

class VariantDataTestUnit : VariantTestCase() {
    override fun apply(variant: Variant) {
        assertNotNull(AGP.run { variant.variantData })
    }
}

class OriginalApplicationIdTestUnit : VariantTestCase() {
    override fun apply(variant: Variant) {
        variant.project.gradle.taskGraph.whenReady {
            assertNotNull(AGP.run { variant.originalApplicationId })
        }
    }
}

class HasDynamicFeatureTestUnit : VariantTestCase() {
    override fun apply(variant: Variant) {
        assertFalse(AGP.run { variant.hasDynamicFeature })
    }
}

class RawAndroidResourcesTestUnit : VariantTestCase() {
    override fun apply(variant: Variant) {
        variant.project.gradle.taskGraph.whenReady {
            val rawAndroidResources = AGP.run { variant.rawAndroidResources }
            assertNotNull(rawAndroidResources)
            if (rawAndroidResources.isEmpty) {
                fail("rawAndroidResources is empty")
            }
            rawAndroidResources.forEach {
                println(" - ${it.path}")
            }
        }
    }
}

class AllArtifactsTestUnit : VariantTestCase() {
    override fun apply(variant: Variant) {
        variant.project.gradle.taskGraph.whenReady {
            val allArtifacts = AGP.run { variant.allArtifacts }
            assertNotNull(allArtifacts)
            if (allArtifacts.isEmpty()) {
                fail("allArtifacts is empty")
            }
            allArtifacts.forEach { (k, v) ->
                println(" - $k => $v")
            }
        }
    }
}

class MinSdkVersionTestUnit : VariantTestCase() {
    override fun apply(variant: Variant) {
        val minSdkVersion = AGP.run { variant.minSdkVersion }
        assertNotNull(minSdkVersion)
        assertEquals(MIN_SDK_VERSION, minSdkVersion.apiLevel)
    }
}

class TargetSdkVersionTestUnit : VariantTestCase() {
    override fun apply(variant: Variant) {
        val targetSdkVersion = AGP.run { variant.targetSdkVersion }
        assertNotNull(targetSdkVersion)
        assertEquals(TARGET_SDK_VERSION, targetSdkVersion.apiLevel)
    }
}

class AarTestUnit : VariantTestCase() {
    override fun apply(project: Project) {
        if (project.plugins.hasPlugin("com.android.library")) {
            super.apply(project)
        }
    }

    override fun apply(variant: Variant) {
        variant.project.gradle.taskGraph.whenReady {
            val aar = AGP.run { variant.aar }.search {
                it.extension == "aar"
            }
            if (aar.isEmpty()) {
                fail("aar is empty")
            }
            aar.forEach {
                println(" - ${it.path}")
            }
        }
    }
}

class ApkTestUnit : VariantTestCase() {
    override fun apply(project: Project) {
        if (project.plugins.hasPlugin("com.android.application")) {
            super.apply(project)
        }
    }

    override fun apply(variant: Variant) {
        variant.project.gradle.taskGraph.whenReady {
            val apk = AGP.run { variant.apk }.search {
                it.extension == "apk"
            }
            if (apk.isEmpty()) {
                fail("apk is empty")
            }
            apk.forEach {
                println(" - ${it.path}")
            }
        }
    }
}

class MergedManifestsTestUnit : VariantTestCase() {
    override fun apply(project: Project) {
        if (project.plugins.hasPlugin("com.android.application")) {
            super.apply(project)
        }
    }

    override fun apply(variant: Variant) {
        variant.project.gradle.taskGraph.whenReady {
            val mergedManifests = AGP.run { variant.mergedManifests }.search {
                it.name == "AndroidManifest.xml"
            }
            if (mergedManifests.isEmpty()) {
                fail("mergedManifests is empty")
            }
            mergedManifests.forEach {
                println(" - ${it.path}")
            }
        }
    }
}

class MergedResourcesTestUnit : VariantTestCase() {
    override fun apply(variant: Variant) {
        variant.project.gradle.taskGraph.whenReady {
            val mergedResources = AGP.run { variant.mergedRes }
            if (mergedResources.isEmpty) {
                fail("mergedRes is empty")
            }
            mergedResources.forEach {
                println(" - ${it.path}")
            }
        }
    }
}

class MergedAssetsTestUnit : VariantTestCase() {
    override fun apply(variant: Variant) {
        variant.project.gradle.taskGraph.whenReady {
            val mergedAssets = AGP.run { variant.mergedAssets }
            if (mergedAssets.isEmpty()) {
                fail("mergedAssets is empty")
            }
            mergedAssets.forEach {
                println(" - ${it.path}")
            }
        }
    }
}

class ProcessedResTestUnit : VariantTestCase() {
    override fun apply(project: Project) {
        if (project.plugins.hasPlugin("com.android.application")) {
            super.apply(project)
        }
    }

    override fun apply(variant: Variant) {
        variant.project.gradle.taskGraph.whenReady {
            val processedRes = AGP.run { variant.processedRes }.search {
                it.extension == "ap_"
            }
            if (processedRes.isEmpty()) {
                fail("processedRes is empty")
            }
            processedRes.forEach {
                println(" - ${it.path}")
            }
        }
    }
}

class SymbolListTestUnit : VariantTestCase() {
    override fun apply(variant: Variant) {
        variant.project.gradle.taskGraph.whenReady {
            val symbolList = AGP.run { variant.symbolList }
            if (symbolList.isEmpty) {
                fail("symbolList is empty")
            }
            symbolList.forEach {
                println(" - ${it.path}")
            }
        }
    }
}

class SymbolListWithPackageNameTestUnit : VariantTestCase() {
    override fun apply(variant: Variant) {
        variant.project.gradle.taskGraph.whenReady {
            val symbolListWithPackageName = AGP.run { variant.symbolListWithPackageName }
            if (symbolListWithPackageName.isEmpty) {
                fail("symbolListWithPackageName is empty")
            }
            symbolListWithPackageName.forEach {
                println(" - ${it.path}")
            }
        }
    }
}

class AllClassesTestUnit : VariantTestCase() {
    override fun apply(variant: Variant) {
        variant.project.gradle.taskGraph.whenReady {
            variant.assembleTaskProvider.configure {
                it.doFirst {
                    val location = AGP.run { variant.allClasses }.files
                    assertTrue("ALL_CLASSES: $location", location::isNotEmpty)
                    assertTrue("No class file found at $location") {
                        location.search(File::isFile).isNotEmpty()
                    }
                }
            }
        }
    }
}
