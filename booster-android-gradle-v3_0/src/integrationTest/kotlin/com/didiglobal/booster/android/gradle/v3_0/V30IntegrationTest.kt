package com.didiglobal.booster.android.gradle.v3_0

import com.android.build.gradle.AppExtension
import com.android.build.gradle.BaseExtension
import com.android.build.gradle.LibraryExtension
import com.android.build.gradle.api.BaseVariant
import com.android.builder.core.VariantType
import com.android.sdklib.BuildToolInfo
import com.didiglobal.booster.gradle.getAndroid
import com.didiglobal.booster.kotlinx.search
import io.bootstage.testkit.gradle.Case
import io.bootstage.testkit.gradle.TestCase
import io.bootstage.testkit.gradle.VariantTestCase
import io.bootstage.testkit.gradle.rules.GradleExecutor
import io.bootstage.testkit.gradle.rules.LocalProperties
import io.bootstage.testkit.gradle.rules.TestCaseConfigure
import io.bootstage.testkit.gradle.rules.copyFromResource
import io.bootstage.testkit.gradle.rules.rule
import org.gradle.api.Project
import org.junit.Before
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import org.junit.rules.TestRule
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.fail

private const val MIN_SDK_VERSION = 18

private const val TARGET_SDK_VERSION = 30

private val AGP = V30

private val ARGS = arrayOf(
        "assemble", "-S",
        "-Pbooster_version=${Build.VERSION}",
        "-Pandroid_gradle_version=3.0.0",
        "-Pcompile_sdk_version=28",
        "-Pbuild_tools_version=26.0.3",
        "-Pmin_sdk_version=${MIN_SDK_VERSION}",
        "-Ptarget_sdk_version=${TARGET_SDK_VERSION}"
)

@Suppress("RemoveCurlyBracesFromTemplate", "FunctionName")
abstract class V30IntegrationTest(val isLib: Boolean) {

    private val projectDir = TemporaryFolder()

    @get:Rule
    val ruleChain: TestRule = rule(projectDir) {
        rule(LocalProperties(projectDir::getRoot)) {
            rule(TestCaseConfigure(projectDir::getRoot)) {
                GradleExecutor(projectDir::getRoot, "4.1", *ARGS)
            }
        }
    }

    @Before
    fun setup() {
        projectDir.copyFromResource("${if (isLib) "lib" else "app"}.gradle", "build.gradle")
        projectDir.copyFromResource("buildSrc")
        projectDir.copyFromResource("src")
    }

    @Test
    @Case(ScopeFullWithFeaturesTest::class)
    fun `test AGPInterface#scopeFullWithFeatures`() {
    }

    @Test
    @Case(ScopeFullLibraryWithFeaturesTest::class)
    fun `test AGPInterface#scopeFullLibraryWithFeatures`() {
    }

    @Test
    @Case(ProjectTest::class)
    fun `test AGPInterface#project`() {
    }

    @Test
    @Case(JavaCompilerTaskTestCase::class)
    fun `test AGPInterface#javaCompilerTask`() {
    }

    @Test
    @Case(PreBuildTaskTestCase::class)
    fun `test AGPInterface#preBuildTask`() {
    }

    @Test
    @Case(AssembleTaskTestCase::class)
    fun `test AGPInterface#assembleTask`() {
    }

    @Test
    @Case(MergeAssetsTaskTestCase::class)
    fun `test AGPInterface#mergeAssetsTask`() {
    }

    @Test
    @Case(MergeResourcesTaskTestCase::class)
    fun `test AGPInterface#mergeResources`() {
    }

    @Test
    @Case(GetTaskNameTestCase::class)
    fun `test AGPInterface#getTaskName(String)`() {
    }

    @Test
    @Case(GetTaskName2TestCase::class)
    fun `test AGPInterface#getTaskName(String, String)`() {
    }

    @Test
    @Case(VariantDataTestCase::class)
    fun `test AGPInterface#variantData`() {
    }

    @Test
    @Case(VariantScopeTestCase::class)
    fun `test AGPInterface#variantScope`() {
    }

    @Test
    @Case(OriginalApplicationIdTestCase::class)
    fun `test AGPInterface#originalApplicationId`() {
    }

    @Test
    @Case(HasDynamicFeatureTestCase::class)
    fun `test AGPInterface#hasDynamicFeature`() {
    }

    @Test
    @Case(RawAndroidResourcesTestCase::class)
    fun `test AGPInterface#rawAndroidResources`() {
    }

    @Test
    @Case(AllArtifactsTestCase::class)
    fun `test AGPInterface#allArtifacts`() {
    }

    @Test
    @Case(MinSdkVersionTestCase::class)
    fun `test AGPInterface#minSdkVersion`() {
    }

    @Test
    @Case(TargetSdkVersionTestCase::class)
    fun `test AGPInterface#targetSdkVersion`() {
    }

    @Test
    @Case(VariantTypeTestCase::class)
    fun `test AGPInterface#variantType`() {
    }

    @Test
    @Case(AarTestCase::class)
    fun `test AGPInterface#aar`() {
    }

    @Test
    @Case(ApkTestCase::class)
    fun `test AGPInterface#apk`() {
    }

    @Test
    @Case(MergedManifestsTestCase::class)
    fun `test AGPInterface#mergedManifests`() {
    }

    @Test
    @Case(MergedResourcesTestCase::class)
    fun `test AGPInterface#mergedRes`() {
    }

    @Test
    @Case(MergedAssetsTestCase::class)
    fun `test AGPInterface#mergedAssets`() {
    }

    @Test
    @Case(ProcessedResTestCase::class)
    fun `test AGPInterface#processedRes`() {
    }

    @Test
    @Case(SymbolListTestCase::class)
    fun `test AGPInterface#symbolList`() {
    }

    @Test
    @Case(SymbolListWithPackageNameTestCase::class)
    fun `test AGPInterface#symbolListWithPackageName`() {
    }

    @Test
    @Case(AllClassesTestCase::class)
    fun `test AGPInterface#allClasses`() {
    }

    @Test
    @Case(BuildToolsTestCase::class)
    fun `test AGPInterface#buildTools`() {
    }

}

class V30AppIntegrationTest : V30IntegrationTest(false)

class V30LibIntegrationTest : V30IntegrationTest(true)

class ScopeFullWithFeaturesTest : TestCase {
    override fun apply(project: Project) {
        assertEquals("PROJECT,SUB_PROJECTS,EXTERNAL_LIBRARIES", AGP.scopeFullWithFeatures.joinToString(","))
    }
}

class ScopeFullLibraryWithFeaturesTest : TestCase {
    override fun apply(project: Project) {
        assertEquals("PROJECT", AGP.scopeFullLibraryWithFeatures.joinToString(","))
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

class JavaCompilerTaskTestCase : VariantTestCase() {
    override fun apply(variant: BaseVariant) {
        assertNotNull(AGP.run { variant.javaCompilerTask })
    }
}

class PreBuildTaskTestCase : VariantTestCase() {
    override fun apply(variant: BaseVariant) {
        assertNotNull(AGP.run { variant.preBuildTask })
    }
}

class AssembleTaskTestCase : VariantTestCase() {
    override fun apply(variant: BaseVariant) {
        assertNotNull(AGP.run { variant.assembleTask })
    }
}

class MergeAssetsTaskTestCase : VariantTestCase() {
    override fun apply(variant: BaseVariant) {
        assertNotNull(AGP.run { variant.mergeAssetsTask })
    }
}

class MergeResourcesTaskTestCase : VariantTestCase() {
    override fun apply(variant: BaseVariant) {
        assertNotNull(AGP.run { variant.mergeResourcesTask })
    }
}

class GetTaskNameTestCase : VariantTestCase() {
    override fun apply(variant: BaseVariant) {
        assertEquals("assemble${variant.name.capitalize()}", AGP.run { variant.getTaskName("assemble") })
    }
}


class GetTaskName2TestCase : VariantTestCase() {
    override fun apply(variant: BaseVariant) {
        assertEquals("merge${variant.name.capitalize()}Resources", AGP.run { variant.getTaskName("merge", "Resources") })
    }
}

class VariantDataTestCase : VariantTestCase() {
    override fun apply(variant: BaseVariant) {
        assertNotNull(AGP.run { variant.variantData })
    }

}

class VariantScopeTestCase : VariantTestCase() {
    override fun apply(variant: BaseVariant) {
        assertNotNull(AGP.run { variant.variantScope })
    }

}

class OriginalApplicationIdTestCase : VariantTestCase() {
    override fun apply(variant: BaseVariant) {
        assertNotNull(AGP.run { variant.originalApplicationId })
    }
}

class HasDynamicFeatureTestCase : VariantTestCase() {
    override fun apply(variant: BaseVariant) {
        assertFalse(AGP.run { variant.hasDynamicFeature })
    }
}

class RawAndroidResourcesTestCase : VariantTestCase() {
    override fun apply(variant: BaseVariant) {
        variant.assemble.doFirst {
            val rawAndroidResources = AGP.run { variant.rawAndroidResources }
            assertNotNull(rawAndroidResources)
            if (rawAndroidResources.isEmpty()) {
                fail("rawAndroidResources is empty")
            }
            rawAndroidResources.forEach {
                println(" - ${it.path}")
            }
        }
    }
}

class AllArtifactsTestCase : VariantTestCase() {
    override fun apply(variant: BaseVariant) {
        variant.assemble.doFirst {
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

class MinSdkVersionTestCase : VariantTestCase() {
    override fun apply(variant: BaseVariant) {
        val minSdkVersion = AGP.run { variant.minSdkVersion }
        assertNotNull(minSdkVersion)
        assertEquals(MIN_SDK_VERSION, minSdkVersion.apiLevel)
    }
}

class TargetSdkVersionTestCase : VariantTestCase() {
    override fun apply(variant: BaseVariant) {
        val targetSdkVersion = AGP.run { variant.targetSdkVersion }
        assertNotNull(targetSdkVersion)
        assertEquals(TARGET_SDK_VERSION, targetSdkVersion.apiLevel)
    }
}

class VariantTypeTestCase : VariantTestCase() {
    override fun apply(variant: BaseVariant) {
        val project = AGP.run { variant.project }
        val variantType = AGP.run { variant.variantType }
        if (project.plugins.hasPlugin("com.android.application")) {
            assertEquals(VariantType.DEFAULT, variantType)
        } else if (project.plugins.hasPlugin("com.android.library")) {
            assertEquals(VariantType.LIBRARY, variantType)
        }
    }
}

class AarTestCase : VariantTestCase() {
    override fun apply(project: Project) {
        if (project.plugins.hasPlugin("com.android.library")) {
            super.apply(project)
        }
    }

    override fun apply(variant: BaseVariant) {
        variant.assemble.doFirst {
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

class ApkTestCase : VariantTestCase() {
    override fun apply(project: Project) {
        if (project.plugins.hasPlugin("com.android.application")) {
            super.apply(project)
        }
    }

    override fun apply(variant: BaseVariant) {
        variant.assemble.doFirst {
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

class MergedManifestsTestCase : VariantTestCase() {
    override fun apply(variant: BaseVariant) {
        variant.assemble.doFirst {
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

class MergedResourcesTestCase : VariantTestCase() {
    override fun apply(variant: BaseVariant) {
        variant.assemble.doFirst {
            val mergedResources = AGP.run { variant.mergedRes }
            if (mergedResources.isEmpty()) {
                fail("mergedRes is empty")
            }
            mergedResources.forEach {
                println(" - ${it.path}")
            }
        }
    }
}

class MergedAssetsTestCase : VariantTestCase() {
    override fun apply(variant: BaseVariant) {
        variant.assemble.doFirst {
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

class ProcessedResTestCase : VariantTestCase() {
    override fun apply(project: Project) {
        if (project.plugins.hasPlugin("com.android.application")) {
            super.apply(project)
        }
    }

    override fun apply(variant: BaseVariant) {
        variant.assemble.doFirst {
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

class SymbolListTestCase : VariantTestCase() {
    override fun apply(variant: BaseVariant) {
        variant.assemble.doFirst {
            val symbolList = AGP.run { variant.symbolList }
            if (symbolList.isEmpty()) {
                fail("symbolList is empty")
            }
            symbolList.forEach {
                println(" - ${it.path}")
            }
        }
    }
}

class SymbolListWithPackageNameTestCase : VariantTestCase() {
    override fun apply(variant: BaseVariant) {
        variant.assemble.doFirst {
            val symbolListWithPackageName = AGP.run { variant.symbolListWithPackageName }
            if (symbolListWithPackageName.isEmpty()) {
                fail("symbolListWithPackageName is empty")
            }
            symbolListWithPackageName.forEach {
                println(" - ${it.path}")
            }
        }
    }
}

class AllClassesTestCase : VariantTestCase() {
    override fun apply(variant: BaseVariant) {
        variant.assemble.doFirst {
            val allClasses = AGP.run { variant.allClasses }.search {
                it.extension == "class"
            }
            if (allClasses.isEmpty()) {
                fail("allClasses is empty")
            }
            allClasses.forEach {
                println(" - ${it.path}")
            }
        }
    }
}

class BuildToolsTestCase : VariantTestCase() {
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
