@file:Suppress("DEPRECATION")

package com.didiglobal.booster.android.gradle.v3_6

import com.android.build.gradle.AppExtension
import com.android.build.gradle.BaseExtension
import com.android.build.gradle.LibraryExtension
import com.android.build.gradle.api.BaseVariant
import com.android.sdklib.BuildToolInfo
import com.didiglobal.booster.gradle.AGP
import com.didiglobal.booster.gradle.getAndroid
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

private const val TARGET_SDK_VERSION = 26

private val ARGS = System.getProperty("gradle.args").split("\\s+".toRegex()) + listOf(
        "-Pbooster_version=${Build.VERSION}",
        "-Pandroid_gradle_version=3.6.0",
        "-Pcompile_sdk_version=28",
        "-Pbuild_tools_version=26.0.3",
        "-Pmin_sdk_version=${MIN_SDK_VERSION}",
        "-Ptarget_sdk_version=${TARGET_SDK_VERSION}"
)

@Suppress("RemoveCurlyBracesFromTemplate", "FunctionName")
abstract class V36IntegrationTest(private val isLib: Boolean) {

    private val projectDir = TemporaryFolder()

    @get:Rule
    val ruleChain: TestRule = rule(projectDir) {
        rule(LocalProperties(projectDir::getRoot)) {
            GradleExecutor(projectDir::getRoot, "5.6.4", *ARGS.toTypedArray())
        }
    }

    @Before
    fun setup() {
        projectDir.copyFromResource("${if (isLib) "lib" else "app"}.gradle", "build.gradle")
        projectDir.copyFromResource("buildSrc")
        projectDir.copyFromResource("src")
        projectDir.newFile("gradle.properties").writeText("org.gradle.jvmargs=-Xmx4g -XX:MaxMetaspaceSize=2g")
        assertEquals(3, AGP.revision.major)
        assertEquals(6, AGP.revision.minor)
    }

    @Test
    @Case(ScopeFullWithFeaturesTest::class)
    fun `test AGPInterface#scopeFullWithFeatures`() = Unit

    @Test
    @Case(ScopeFullLibraryWithFeaturesTest::class)
    fun `test AGPInterface#scopeFullLibraryWithFeatures`() = Unit

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
    @Case(GetTaskNameTestUnit::class)
    fun `test AGPInterface#getTaskName(String)`() = Unit

    @Test
    @Case(GetTaskName2TestUnit::class)
    fun `test AGPInterface#getTaskName(String, String)`() = Unit

    @Test
    @Case(VariantDataTestUnit::class)
    fun `test AGPInterface#variantData`() = Unit

    @Test
    @Case(VariantScopeTestUnit::class)
    fun `test AGPInterface#variantScope`() = Unit

    @Test
    @Case(OriginalApplicationIdTestUnit::class)
    fun `test AGPInterface#originalApplicationId`() = Unit

    @Test
    @Case(HasDynamicFeatureTestUnit::class)
    fun `test AGPInterface#hasDynamicFeature`() = Unit

    @Test
    @Case(RawAndroidResourcesTestUnit::class)
    fun `test AGPInterface#rawAndroidResources`() = Unit

    @Test
    @Case(AllArtifactsTestUnit::class)
    fun `test AGPInterface#allArtifacts`() = Unit

    @Test
    @Case(MinSdkVersionTestUnit::class)
    fun `test AGPInterface#minSdkVersion`() = Unit

    @Test
    @Case(TargetSdkVersionTestUnit::class)
    fun `test AGPInterface#targetSdkVersion`() = Unit

    @Test
    @Case(AarTestUnit::class)
    fun `test AGPInterface#aar`() = Unit

    @Test
    @Case(ApkTestUnit::class)
    fun `test AGPInterface#apk`() = Unit

    @Test
    @Case(MergedManifestsTestUnit::class)
    fun `test AGPInterface#mergedManifests`() = Unit

    @Test
    @Case(MergedResourcesTestUnit::class)
    fun `test AGPInterface#mergedRes`() = Unit

    @Test
    @Case(MergedAssetsTestUnit::class)
    fun `test AGPInterface#mergedAssets`() = Unit

    @Test
    @Case(ProcessedResTestUnit::class)
    fun `test AGPInterface#processedRes`() = Unit

    @Test
    @Case(SymbolListTestUnit::class)
    fun `test AGPInterface#symbolList`() = Unit

    @Test
    @Case(SymbolListWithPackageNameTestUnit::class)
    fun `test AGPInterface#symbolListWithPackageName`() = Unit

    @Test
    @Case(AllClassesTestUnit::class)
    fun `test AGPInterface#allClasses`() = Unit

    @Test
    @Case(BuildToolsTestUnit::class)
    fun `test AGPInterface#buildTools`() = Unit

}

class V36AppIntegrationTest : V36IntegrationTest(false)

class V36LibIntegrationTest : V36IntegrationTest(true)

class ScopeFullWithFeaturesTest : TestCase {
    override fun apply(project: Project) {
        assertEquals("PROJECT,SUB_PROJECTS,EXTERNAL_LIBRARIES,FEATURES", AGP.scopeFullWithFeatures.joinToString(","))
    }
}

class ScopeFullLibraryWithFeaturesTest : TestCase {
    override fun apply(project: Project) {
        assertEquals("FEATURES,PROJECT", AGP.scopeFullLibraryWithFeatures.joinToString(","))
    }
}

class ProjectTest : TestCase {
    override fun apply(project: Project) {
        val assert: (BaseVariant) -> Unit = { variant ->
            assertEquals(project, AGP.run { variant.project })
        }
        project.afterEvaluate {
            when (val android = project.getAndroid<BaseExtension>()) {
                is AppExtension -> android.applicationVariants.forEach(assert)
                is LibraryExtension -> android.libraryVariants.forEach(assert)
            }
        }
    }
}

class JavaCompilerTaskTestUnit : VariantTestCase() {
    override fun apply(variant: BaseVariant) {
        assertNotNull(AGP.run { variant.javaCompilerTask })
    }
}

class PreBuildTaskTestUnit : VariantTestCase() {
    override fun apply(variant: BaseVariant) {
        assertNotNull(AGP.run { variant.preBuildTask })
    }
}

class AssembleTaskTestUnit : VariantTestCase() {
    override fun apply(variant: BaseVariant) {
        assertNotNull(AGP.run { variant.assembleTask })
    }
}

class MergeAssetsTaskTestUnit : VariantTestCase() {
    override fun apply(variant: BaseVariant) {
        assertNotNull(AGP.run { variant.mergeAssetsTask })
    }
}

class MergeResourcesTaskTestUnit : VariantTestCase() {
    override fun apply(variant: BaseVariant) {
        assertNotNull(AGP.run { variant.mergeResourcesTask })
    }
}

class GetTaskNameTestUnit : VariantTestCase() {
    override fun apply(variant: BaseVariant) {
        assertEquals("assemble${variant.name.capitalize()}", AGP.run { variant.getTaskName("assemble") })
    }
}


class GetTaskName2TestUnit : VariantTestCase() {
    override fun apply(variant: BaseVariant) {
        assertEquals("merge${variant.name.capitalize()}Resources", AGP.run { variant.getTaskName("merge", "Resources") })
    }
}

class VariantDataTestUnit : VariantTestCase() {
    override fun apply(variant: BaseVariant) {
        assertNotNull(AGP.run { variant.variantData })
    }
}

class VariantScopeTestUnit : VariantTestCase() {
    override fun apply(variant: BaseVariant) {
        assertNotNull(AGP.run { variant.variantScope })
    }
}

class OriginalApplicationIdTestUnit : VariantTestCase() {
    override fun apply(variant: BaseVariant) {
        assertNotNull(AGP.run { variant.originalApplicationId })
    }
}

class HasDynamicFeatureTestUnit : VariantTestCase() {
    override fun apply(variant: BaseVariant) {
        assertFalse(AGP.run { variant.hasDynamicFeature })
    }
}

class RawAndroidResourcesTestUnit : VariantTestCase() {
    override fun apply(variant: BaseVariant) {
        AGP.run { variant.assembleTask }.doFirst {
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
    override fun apply(variant: BaseVariant) {
        AGP.run { variant.assembleTask }.doFirst {
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
    override fun apply(variant: BaseVariant) {
        val minSdkVersion = AGP.run { variant.minSdkVersion }
        assertNotNull(minSdkVersion)
        assertEquals(MIN_SDK_VERSION, minSdkVersion.apiLevel)
    }
}

class TargetSdkVersionTestUnit : VariantTestCase() {
    override fun apply(variant: BaseVariant) {
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

    override fun apply(variant: BaseVariant) {
        AGP.run { variant.assembleTask }.doFirst {
            val location = AGP.run { variant.aar }.files
            assertTrue("AAR: $location", location::isNotEmpty)
            assertTrue("No aar found at $location") {
                location.search {
                    it.extension == "aar"
                }.isNotEmpty()
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

    override fun apply(variant: BaseVariant) {
        AGP.run { variant.assembleTask }.doFirst {
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

    override fun apply(variant: BaseVariant) {
        AGP.run { variant.assembleTask }.doFirst {
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
    override fun apply(variant: BaseVariant) {
        AGP.run { variant.assembleTask }.doFirst {
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
    override fun apply(variant: BaseVariant) {
        AGP.run { variant.assembleTask }.doFirst {
            val mergedAssets = AGP.run { variant.mergedAssets }
            if (mergedAssets.isEmpty) {
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

    override fun apply(variant: BaseVariant) {
        AGP.run { variant.assembleTask }.doFirst {
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
    override fun apply(variant: BaseVariant) {
        AGP.run { variant.assembleTask }.doFirst {
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
    override fun apply(variant: BaseVariant) {
        AGP.run { variant.assembleTask }.doFirst {
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
    override fun apply(variant: BaseVariant) {
        AGP.run { variant.assembleTask }.doFirst {
            val location = AGP.run { variant.allClasses }.files
            assertTrue("ALL_CLASSES: $location", location::isNotEmpty)
            assertTrue("No class file found at $location") {
                location.search(File::isFile).isNotEmpty()
            }
        }
    }
}

class BuildToolsTestUnit : VariantTestCase() {
    override fun apply(variant: BaseVariant) {
        val buildTools = AGP.run { variant.buildTools }
        assertNotNull(buildTools)
        BuildToolInfo.PathId.values().map {
            it.name to buildTools.getPath(it)
        }.forEach { (k, v) ->
            println(" - $k => $v")
        }
    }
}
