package com.didiglobal.booster.gradle

import com.didiglobal.booster.transform.CompositeTransformer
import com.didiglobal.booster.transform.TransformContext
import com.didiglobal.booster.transform.TransformListener
import com.didiglobal.booster.transform.TransformScope
import com.didiglobal.booster.transform.Transformer
import org.gradle.api.GradleException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class TransformModeTest {

    private val classScoped = object : Transformer {
        override val scope = TransformScope.CLASS
        override fun transform(context: TransformContext, bytecode: ByteArray) = bytecode
    }

    private val programScoped = object : Transformer {
        override fun transform(context: TransformContext, bytecode: ByteArray) = bytecode
    }

    @Test
    fun `auto chooses instrumentation when all transformers are class scoped`() {
        assertEquals(TRANSFORM_MODE_INSTRUMENTATION, resolveTransformMode(null, listOf(classScoped, classScoped)))
        assertEquals(TRANSFORM_MODE_INSTRUMENTATION, resolveTransformMode(TRANSFORM_MODE_AUTO, listOf(classScoped)))
    }

    @Test
    fun `auto falls back to scoped artifacts when any transformer requires the whole program`() {
        assertEquals(TRANSFORM_MODE_SCOPED_ARTIFACTS, resolveTransformMode(null, listOf(classScoped, programScoped)))
        assertEquals(TRANSFORM_MODE_SCOPED_ARTIFACTS, resolveTransformMode("", listOf(programScoped)))
    }

    @Test
    fun `explicit mode overrides the scopes`() {
        assertEquals(TRANSFORM_MODE_INSTRUMENTATION, resolveTransformMode(TRANSFORM_MODE_INSTRUMENTATION, listOf(programScoped)))
        assertEquals(TRANSFORM_MODE_SCOPED_ARTIFACTS, resolveTransformMode(TRANSFORM_MODE_SCOPED_ARTIFACTS, listOf(classScoped)))
    }

    @Test
    fun `program scope is explained down to the delegates`() {
        val composite = object : CompositeTransformer {
            override val delegates: Iterable<TransformListener> = listOf(classScoped, programScoped)
            override val scope = TransformScope.PROGRAM
            override fun transform(context: TransformContext, bytecode: ByteArray) = bytecode
        }
        val description = describeTransformScope(":app", TRANSFORM_MODE_SCOPED_ARTIFACTS, listOf(classScoped, composite))
        assertTrue(description.contains("scope of :app is PROGRAM"), description)
        assertTrue(description.contains("- ${composite.javaClass.name}: PROGRAM"), description)
        assertTrue(description.contains("    - ${programScoped.javaClass.name}: PROGRAM"), description)
        assertTrue(description.contains("    - ${classScoped.javaClass.name}: CLASS"), description)
        assertTrue(!description.contains("\n  - ${classScoped.javaClass.name}"), description)

        val classDescription = describeTransformScope(":app", TRANSFORM_MODE_INSTRUMENTATION, listOf(classScoped))
        assertTrue(classDescription.contains("scope of :app is CLASS"), classDescription)
        assertTrue(!classDescription.contains("required by"), classDescription)
    }

    @Test
    fun `unknown mode is rejected`() {
        assertFailsWith<GradleException> {
            resolveTransformMode("jar", listOf(classScoped))
        }
    }

}
