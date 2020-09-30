package com.didiglobal.booster.kotlinx

import java.io.File
import java.util.concurrent.RecursiveTask

/**
 * Find files from the specified paths
 *
 * @author johnsonlee
 */
class FileSearch internal constructor(private val roots: Iterable<File>, private val filter: (File) -> Boolean = { true }) : RecursiveTask<Collection<File>>() {

    internal constructor(roots: Array<File>, filter: (File) -> Boolean = { true }) : this(roots.toList(), filter)

    internal constructor(root: File, filter: (File) -> Boolean = { true }) : this(listOf(root), filter)

    override fun compute(): Collection<File> {
        val tasks = mutableListOf<RecursiveTask<Collection<File>>>()
        val result = mutableSetOf<File>()

        roots.forEach { root ->
            if (root.isDirectory) {
                root.listFiles()?.let { files ->
                    FileSearch(files, filter).also { task ->
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

@JvmOverloads
fun File.search(filter: (File) -> Boolean = { true }): Collection<File> = FileSearch(this, filter).execute()

@JvmOverloads
fun Iterable<File>.search(filter: (File) -> Boolean = { true }): Collection<File> = FileSearch(this, filter).execute()

@JvmOverloads
fun Iterator<File>.search(filter: (File) -> Boolean = { true }): Collection<File> = FileSearch(this.asIterable(), filter).execute()

@JvmOverloads
fun Array<File>.search(filter: (File) -> Boolean = { true }): Collection<File> = FileSearch(this, filter).execute()
