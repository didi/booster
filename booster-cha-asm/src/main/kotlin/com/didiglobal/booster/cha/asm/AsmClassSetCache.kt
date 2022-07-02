package com.didiglobal.booster.cha.asm

import com.didiglobal.booster.cha.ClassSet
import com.didiglobal.booster.cha.ClassSetCache
import org.objectweb.asm.tree.ClassNode
import java.io.File
import java.net.URL
import java.util.concurrent.ConcurrentHashMap

typealias AsmClassSetLoader = (URL) -> AsmClassSet

class AsmClassSetCache(
        private val loader: AsmClassSetLoader = { ClassSet.from(File(it.file)) }
) : ClassSetCache<ClassNode, AsmClassFileParser> {

    private val cache = ConcurrentHashMap<URL, AsmClassSet>()

    override fun get(url: URL): AsmClassSet {
        return cache[url] ?: loader(url).also {
            cache[url] = it
        }
    }

}