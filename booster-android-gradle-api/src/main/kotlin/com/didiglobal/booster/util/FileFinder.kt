package com.didiglobal.booster.util

import java.io.File
import java.util.concurrent.RecursiveTask

/**
 * Find files from the specified paths
 *
 * @author johnsonlee
 */
class FileFinder(private val roots: Collection<File>, private val filter: (File) -> Boolean = { true }) : RecursiveTask<Collection<File>>() {

    constructor(roots: Array<File>, filter: (File) -> Boolean = { true }) : this(roots.toList(), filter)

    constructor(root: File, filter: (File) -> Boolean = { true }) : this(listOf(root), filter)

    override fun compute(): Collection<File> {
        val tasks = mutableListOf<RecursiveTask<Collection<File>>>()
        val result = mutableSetOf<File>()

        roots.forEach { root ->
            if (root.isDirectory) {
                root.listFiles()?.let { files ->
                    FileFinder(files, filter).also { task ->
                        tasks.add(task)
                    }.fork()
                }
            } else if (root.isFile) {
                if (filter.invoke(root)) {
                    result.add(root)
                }
            }
        }

        return result + tasks.flatMap { it.join() }
    }

}
