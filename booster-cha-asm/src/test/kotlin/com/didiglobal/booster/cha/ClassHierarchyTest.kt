package com.didiglobal.booster.cha

import com.didiglobal.booster.build.AndroidSdk
import com.didiglobal.booster.cha.asm.from
import java.time.Duration
import kotlin.test.Test
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class ClassHierarchyTest {

    @Test
    fun `lookup children class of Context`() {
        val androidSdk =  ClassSet.from(AndroidSdk.getAndroidJar())

        // build class hierarchy
        val t1 = System.nanoTime()
        val hierarchy = ClassHierarchy(androidSdk)
        println("t1: ${Duration.ofNanos(System.nanoTime() - t1).toMillis()} ms")

        // acquire subtypes of Context
        val t2 = System.nanoTime()
        val childrenOfContext = hierarchy.getDerivedTypes("android/content/Context")
        println("t2 : ${Duration.ofNanos(System.nanoTime() - t2).toMillis()} ms")
        assertNotNull(childrenOfContext)
        assertTrue("Children of android/content/Context not found") {
            childrenOfContext.isNotEmpty()
        }
        println(childrenOfContext.joinToString(", ") { it.name })

        // acquire subtypes of Context
        val t3 = System.nanoTime()
        val childrenOfThread = hierarchy.getDerivedTypes("java/lang/Thread")
        println("t3 : ${Duration.ofNanos(System.nanoTime() - t3).toMillis()} ms")
        println(childrenOfThread.joinToString(", ") { it.name })
    }

}