package com.didiglobal.booster.transform.shrink

import java.io.File
import java.util.concurrent.RecursiveTask

internal class RCollector(private val root: File) : RecursiveTask<Collection<File>>() {

    override fun compute(): Collection<File> {
        val tasks = mutableListOf<RecursiveTask<Collection<File>>>()
        val result = mutableListOf<File>()

        root.listFiles()?.forEach { file ->
            if (file.isDirectory) {
                RCollector(file).also { task ->
                    tasks.add(task)
                }.fork()
            } else if ("R.class" == file.name || (file.name.startsWith("R$") && file.name.endsWith(".class"))) {
                result.add(file)
            }
        }

        return result + tasks.flatMap { it.join() }
    }

}
