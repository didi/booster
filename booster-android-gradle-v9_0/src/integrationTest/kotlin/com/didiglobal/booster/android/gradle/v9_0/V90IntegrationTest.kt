@file:Suppress("DEPRECATION")

package com.didiglobal.booster.android.gradle.v9_0

import com.android.build.api.variant.Variant
import com.android.build.api.artifact.SingleArtifact
import com.android.build.gradle.internal.publishing.AndroidArtifacts
import com.didiglobal.booster.gradle.AGP
import com.didiglobal.booster.gradle.aar
import com.didiglobal.booster.gradle.allArtifacts
import com.didiglobal.booster.gradle.allClasses
import com.didiglobal.booster.gradle.apk
import com.didiglobal.booster.gradle.assembleTaskProvider
import com.didiglobal.booster.gradle.buildTools
import com.didiglobal.booster.gradle.getArtifactCollection
import com.didiglobal.booster.gradle.getArtifactFileCollection
import com.didiglobal.booster.gradle.getSingleArtifact
import com.didiglobal.booster.gradle.getTaskName
import com.didiglobal.booster.gradle.isApplication
import com.didiglobal.booster.gradle.isDebuggable
import com.didiglobal.booster.gradle.isDynamicFeature
import com.didiglobal.booster.gradle.isLibrary
import com.didiglobal.booster.gradle.isPrecompileDependenciesResourcesEnabled
import com.didiglobal.booster.gradle.localAndroidResources
import com.didiglobal.booster.gradle.mergeAssetsTaskProvider
import com.didiglobal.booster.gradle.mergeNativeLibsTaskProvider
import com.didiglobal.booster.gradle.mergeResourcesTaskProvider
import com.didiglobal.booster.gradle.mergedAssets
import com.didiglobal.booster.gradle.mergedManifests
import com.didiglobal.booster.gradle.mergedNativeLibs
import com.didiglobal.booster.gradle.mergedRes
import com.didiglobal.booster.gradle.originalApplicationId
import com.didiglobal.booster.gradle.preBuildTaskProvider
import com.didiglobal.booster.gradle.processJavaResourcesTaskProvider
import com.didiglobal.booster.gradle.processedRes
import com.didiglobal.booster.gradle.project
import com.didiglobal.booster.gradle.symbolList
import com.didiglobal.booster.gradle.symbolListWithPackageName
import com.didiglobal.booster.gradle.targetVersion
import com.didiglobal.booster.kotlinx.search
import io.bootstage.testkit.gradle.Case
import io.bootstage.testkit.gradle.VariantTestCase
import io.bootstage.testkit.gradle.rules.GradleExecutor
import io.bootstage.testkit.gradle.rules.LocalProperties
import io.bootstage.testkit.gradle.rules.copyFromResource
import io.bootstage.testkit.gradle.rules.rule
import org.junit.Before
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import org.junit.rules.TestRule
import java.io.File
import java.util.zip.ZipFile
import kotlin.test.*

private val MIN_SDK_VERSION = System.getProperty("android.minsdk.version").toInt()
private const val TARGET_SDK_VERSION = 30

private val ARGS = System.getProperty("gradle.args").split("\\s+".toRegex()) + listOf(
    "-Pbooster_version=${Build.VERSION}",
    "-Pandroid_gradle_version=9.0.1",
    "-Pcompile_sdk_version=35",
    "-Pbuild_tools_version=35.0.0",
    "-Pmin_sdk_version=$MIN_SDK_VERSION",
    "-Ptarget_sdk_version=$TARGET_SDK_VERSION",
    "-Dorg.gradle.internal.instrumentation.agent=false"
) + System.getProperty("jacoco.jvmarg")?.let { agentArgument ->
    listOf("-Dorg.gradle.jvmargs=-Xmx4g -XX:MaxMetaspaceSize=2g $agentArgument")
}.orEmpty()

@Suppress("RemoveCurlyBracesFromTemplate", "FunctionName")
abstract class V90IntegrationTest(private val isLib: Boolean) {

    private val projectDir = TemporaryFolder()

    @get:Rule
    val ruleChain: TestRule = rule(projectDir) {
        rule(LocalProperties(projectDir::getRoot)) {
            GradleExecutor(projectDir::getRoot, "9.1.0", *ARGS.toTypedArray())
        }
    }

    @Before
    fun setup() {
        projectDir.copyFromResource("${if (isLib) "lib" else "app"}.gradle", "build.gradle")
        projectDir.copyFromResource("buildSrc")
        projectDir.copyFromResource("src")
        projectDir.newFile("gradle.properties").writeText("org.gradle.jvmargs=-Xmx4g -XX:MaxMetaspaceSize=2g")
        assertEquals(9, AGP.revision.major)
        assertEquals(0, AGP.revision.minor)
    }

    @Test
    @Case(AGP90CompatibilityTestUnit::class)
    fun `test AGP 9 compatibility surface`() = Unit

}

class V90AppIntegrationTest : V90IntegrationTest(false)

class V90LibIntegrationTest : V90IntegrationTest(true)


class AGP90CompatibilityTestUnit : VariantTestCase() {
    override fun apply(variant: Variant) {
        AGP.run {
            assertEquals(variant.project, variant.project)
            assertTrue(variant.getTaskName("verify").startsWith("verify"))
            assertTrue(variant.getTaskName("verify", "Compatibility").endsWith("Compatibility"))
            assertEquals("com.didiglobal.booster.android.test", variant.originalApplicationId)
            assertEquals(variant.debuggable, variant.isDebuggable)
            assertEquals(variant is com.android.build.api.variant.ApplicationVariant, variant.isApplication)
            assertEquals(variant is com.android.build.api.variant.LibraryVariant, variant.isLibrary)
            assertFalse(variant.isDynamicFeature)
            assertFalse(variant.hasDynamicFeature)
            assertNotNull(variant.variantData)
            assertTrue(variant.targetVersion.apiLevel > 0)
            assertNotNull(variant.preBuildTaskProvider)
            assertNotNull(variant.javaCompilerTaskProvider)
            assertNotNull(variant.assembleTaskProvider)
            assertNotNull(variant.mergeAssetsTaskProvider)
            assertNotNull(variant.mergeResourcesTaskProvider)
            assertNotNull(variant.mergeNativeLibsTaskProvider)
            assertNotNull(variant.processJavaResourcesTaskProvider)
            assertNotNull(variant.getSingleArtifact(SingleArtifact.MERGED_MANIFEST))
            assertNotNull(variant.getArtifactCollection(
                    AndroidArtifacts.ConsumedConfigType.RUNTIME_CLASSPATH,
                    AndroidArtifacts.ArtifactScope.ALL,
                    AndroidArtifacts.ArtifactType.CLASSES_JAR
            ))
            assertNotNull(variant.getArtifactFileCollection(
                    AndroidArtifacts.ConsumedConfigType.RUNTIME_CLASSPATH,
                    AndroidArtifacts.ArtifactScope.ALL,
                    AndroidArtifacts.ArtifactType.CLASSES_JAR
            ))
            assertTrue(variant.allArtifacts.isNotEmpty())
            variant.getDependencies(transitive = true)
            variant.getDependencies(transitive = false)
            variant.isPrecompileDependenciesResourcesEnabled
        }

        variant.assembleTaskProvider.configure {
            it.doLast {
                AGP.run {
                    assertNotNull(variant.buildTools)
                    assertTrue(variant.rawAndroidResources.files.isNotEmpty())
                    assertTrue(variant.localAndroidResources.files.isNotEmpty())
                    assertTrue(variant.sourceSetMap.files.isNotEmpty())
                    assertTrue(variant.mergedManifests.files.isNotEmpty())
                    assertTrue(variant.mergedRes.files.isNotEmpty())
                    assertTrue(variant.mergedAssets.files.isNotEmpty())
                    assertTrue(variant.mergedNativeLibs.files.isNotEmpty())
                    assertTrue(variant.processedRes.files.isNotEmpty())
                    assertTrue(variant.symbolList.files.isNotEmpty())
                    assertTrue(variant.symbolListWithPackageName.files.isNotEmpty())
                    variant.dataBindingDependencyArtifacts.files
                    val classes = variant.allClasses.files
                    assertTrue(classes.isNotEmpty(), "ALL_CLASSES: $classes")
                    assertTrue("No class file found at $classes") {
                        classes.search(File::isFile).isNotEmpty()
                    }
                    assertTrue("Built-in Kotlin output is missing from $classes") {
                        classes.any { file ->
                            when {
                                file.isDirectory -> file.search { it.name == "KotlinMarker.class" }.isNotEmpty()
                                file.extension == "jar" -> ZipFile(file).use {
                                    it.getEntry("com/didiglobal/booster/android/test/KotlinMarker.class") != null
                                }
                                else -> file.name == "KotlinMarker.class"
                            }
                        }
                    }
                    if (variant.isApplication) {
                        assertTrue(variant.apk.files.isNotEmpty())
                    } else {
                        assertTrue(variant.aar.files.isNotEmpty())
                    }
                }
            }
        }
    }
}
