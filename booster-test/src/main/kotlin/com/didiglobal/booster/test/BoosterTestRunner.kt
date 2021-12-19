package com.didiglobal.booster.test

import com.didiglobal.booster.transform.util.TransformerClassLoader
import org.junit.runners.BlockJUnit4ClassRunner
import org.junit.runners.model.FrameworkMethod
import org.junit.runners.model.Statement

/**
 * An abstraction of test runner with transformer
 */
abstract class BoosterTestRunner(
        clazz: Class<*>,
        private val classLoader: TransformerClassLoader
) : BlockJUnit4ClassRunner(clazz) {

    private val contextClassLoader = Thread.currentThread().contextClassLoader

    init {
        Thread.currentThread().contextClassLoader = classLoader
    }

    override fun methodBlock(method: FrameworkMethod): Statement = object : Statement() {
        override fun evaluate() {
            val testClazz = classLoader.loadClass(testClass.javaClass.name)
            val testRunner = TestRunnerWrapper(testClazz)
            val testMethod = testClazz.getMethod(method.method.name)

            try {
                testRunner.methodBlock(FrameworkMethod(testMethod)).evaluate()
            } finally {
                Thread.currentThread().contextClassLoader = contextClassLoader
            }
        }
    }

    private class TestRunnerWrapper(clazz: Class<*>) : BlockJUnit4ClassRunner(clazz) {

        public override fun methodBlock(method: FrameworkMethod?): Statement {
            return super.methodBlock(method)
        }

    }

}