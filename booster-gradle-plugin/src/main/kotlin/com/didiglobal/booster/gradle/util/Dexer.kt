package com.didiglobal.booster.gradle.util

import com.android.dex.DexFormat
import com.android.dx.command.dexer.Main
import com.didiglobal.booster.kotlinx.NCPU
import java.io.File

internal fun File.dex(output: File, api: Int = DexFormat.API_NO_EXTENDED_OPCODES): Int {
    val args = Main.Arguments().apply {
        numThreads = NCPU
        debug = true
        warnings = true
        emptyOk = true
        multiDex = true
        jarOutput = true
        optimize = false
        minSdkVersion = api
        fileNames = arrayOf(output.canonicalPath)
        outName = canonicalPath
    }
    return try {
        Main.run(args)
    } catch (t: Throwable) {
        t.printStackTrace()
        -1
    }
}
