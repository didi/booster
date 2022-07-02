package com.didiglobal.booster.cha

import java.net.URL

interface ClassSetCache<ClassFile, ClassParser> where ClassParser : ClassFileParser<ClassFile> {

    operator fun get(url: URL): ClassSet<ClassFile, ClassParser>

}
