package com.didiglobal.booster.kotlinx

import java.io.File
import java.util.concurrent.ForkJoinPool
import java.util.concurrent.RecursiveTask

private val forkJoinPool = ForkJoinPool()

/**
 * Represents a file tree which walk in parallel
 *
 * @author johnsonlee
 */
internal class FileTree(private val root: File) : Iterable<File> {

    override fun iterator() = walk()

    private fun walk(): Iterator<File> {
        return forkJoinPool.invoke(FileWalker(root)).iterator()
    }

    class FileWalker(private val root: File) : RecursiveTask<List<File>>() {

        override fun compute(): List<File> {
            val tasks = mutableListOf<RecursiveTask<List<File>>>()
            val result = mutableListOf<File>()

            root.listFiles()?.forEach { file ->
                result.add(file)
                if (file.isDirectory) {
                    FileWalker(file).also { task ->
                        tasks.add(task)
                    }.fork()
                }
            }

            return result + tasks.flatMap { it.join() }
        }

    }

}
