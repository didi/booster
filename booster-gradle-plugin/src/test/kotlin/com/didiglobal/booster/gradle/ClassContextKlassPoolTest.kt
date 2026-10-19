package com.didiglobal.booster.gradle

import com.android.build.api.instrumentation.ClassContext
import com.android.build.api.instrumentation.ClassData
import com.didiglobal.booster.transform.AbstractKlassPool
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ClassContextKlassPoolTest {

    private val hierarchy = mapOf(
        "java.lang.Thread" to data("java.lang.Thread", superClasses = listOf("java.lang.Object"), interfaces = listOf("java.lang.Runnable")),
        "com.example.Worker" to data("com.example.Worker", superClasses = listOf("java.lang.Thread", "java.lang.Object"), interfaces = listOf("java.lang.Runnable")),
        "com.example.Task" to data("com.example.Task", superClasses = listOf("java.lang.Object"), interfaces = listOf("java.lang.Runnable"))
    )

    private val classContext = object : ClassContext {
        override val currentClassData: ClassData get() = hierarchy.getValue("com.example.Worker")
        override fun loadClassData(className: String): ClassData? = hierarchy[className]
    }

    private val bootPool = object : AbstractKlassPool(emptyList()) {}

    @Test
    fun `resolves hierarchy from class context`() {
        val pool = ClassContextKlassPool(bootPool) { classContext }
        assertTrue(pool["java/lang/Thread"].isAssignableFrom("com/example/Worker"))
        assertTrue(pool["java/lang/Runnable"].isAssignableFrom("com/example/Task"))
        assertTrue(pool["java/lang/Thread"].isAssignableFrom("java/lang/Thread"))
        assertFalse(pool["java/lang/Thread"].isAssignableFrom("com/example/Task"))
        assertFalse(pool["com/example/Worker"].isAssignableFrom("java/lang/Thread"))
    }

    @Test
    fun `falls back to boot class pool without class context`() {
        val pool = ClassContextKlassPool(bootPool) { null }
        assertTrue(pool["java/lang/Object"].isAssignableFrom("java/lang/String"))
        assertTrue(pool["java/lang/Runnable"].isAssignableFrom("java/lang/Thread"))
        assertFalse(pool["java/lang/Thread"].isAssignableFrom("java/lang/String"))
    }

    @Test
    fun `falls back to boot class pool for unknown classes`() {
        val pool = ClassContextKlassPool(bootPool) { classContext }
        assertTrue(pool["java/lang/Object"].isAssignableFrom("java/lang/String"))
        assertFalse(pool["java/lang/Thread"].isAssignableFrom("com/example/Unknown"))
    }

    private fun data(name: String, superClasses: List<String>, interfaces: List<String>) = object : ClassData {
        override val className: String = name
        override val classAnnotations: List<String> = emptyList()
        override val interfaces: List<String> = interfaces
        override val superClasses: List<String> = superClasses
    }

}
