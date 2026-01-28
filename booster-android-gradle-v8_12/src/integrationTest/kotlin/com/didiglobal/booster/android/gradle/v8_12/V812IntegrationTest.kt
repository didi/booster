@file:Suppress("DEPRECATION")

package com.didiglobal.booster.android.gradle.v8_12

import com.android.build.api.variant.AndroidComponentsExtension
import com.android.build.api.variant.Variant
import com.didiglobal.booster.gradle.AGP
import com.didiglobal.booster.gradle.assembleTaskProvider
import com.didiglobal.booster.gradle.getAndroidComponents
import com.didiglobal.booster.gradle.project
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
import kotlin.test.*

private val MIN_SDK_VERSION = System.getProperty("android.minsdk.version").toInt()
private const val TARGET_SDK_VERSION = 30

private val ARGS = System.getProperty("gradle.args").split("\\s+".toRegex()) + listOf(
    "-Pbooster_version=${Build.VERSION}",
    "-Pandroid_gradle_version=8.12.0",
    "-Pcompile_sdk_version=34",
    "-Pbuild_tools_version=34.0.0",
    "-Pmin_sdk_version=$MIN_SDK_VERSION",
    "-Ptarget_sdk_version=$TARGET_SDK_VERSION"
)

@Suppress("RemoveCurlyBracesFromTemplate", "FunctionName")
abstract class V812IntegrationTest(private val isLib: Boolean) {

    private val projectDir = TemporaryFolder()

    @get:Rule
    val ruleChain: TestRule = rule(projectDir) {
        rule(LocalProperties(projectDir::getRoot)) {
            GradleExecutor(projectDir::getRoot, "8.11.1", *ARGS.toTypedArray())
        }
    }

    @Before
    fun setup() {
        projectDir.copyFromResource("${if (isLib) "lib" else "app"}.gradle", "build.gradle")
        projectDir.copyFromResource("buildSrc")
        projectDir.copyFromResource("src")
        projectDir.newFile("gradle.properties").writeText("org.gradle.jvmargs=-Xmx4g -XX:MaxMetaspaceSize=2g")
        assertEquals(8, AGP.revision.major)
        assertEquals(12, AGP.revision.minor)
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
    fun `test AGPInterface#allClasses`() = Unit

}

class V812AppIntegrationTest : V812IntegrationTest(false)

class V812LibIntegrationTest : V812IntegrationTest(true)


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
