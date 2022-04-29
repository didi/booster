package com.didiglobal.booster.gradle.util

import com.android.dex.DexFormat
import com.android.dx.command.dexer.Main
import com.android.tools.r8.D8
import com.didiglobal.booster.gradle.GTE_V7_X
import com.didiglobal.booster.kotlinx.NCPU
import com.didiglobal.booster.kotlinx.search
import com.didiglobal.booster.kotlinx.touch
import java.io.File

internal fun File.dex(output: File, api: Int = DexFormat.API_NO_EXTENDED_OPCODES): Int {
    return if (GTE_V7_X) {
        runD8(this, output, api)
    } else {
        runDx(this, output, api)
    }
}

private fun runD8(input: File, output: File, api: Int): Int {
    val inputs = when {
        input.isDirectory -> {
            output.mkdirs()
            input.search {
                it.extension.equals("class", true)
            }
        }
        else -> {
            output.touch()
            listOf(input)
        }
    }

    val args = inputs.map { it.canonicalPath } + "--min-api" + api.toString()
    return try {
        D8.main(args.toTypedArray())
        0
    } catch (t: Throwable) {
        t.printStackTrace()
        -1
    }
}

private fun runDx(input: File, output: File, api: Int): Int {
    val args = Main.Arguments().apply {
        numThreads = NCPU
        debug = true
        warnings = true
        emptyOk = true
        multiDex = true
        jarOutput = true
        optimize = false
        minSdkVersion = api
        fileNames = arrayOf(input.canonicalPath)
        outName = output.canonicalPath
    }
    return try {
        Main.run(args)
    } catch (t: Throwable) {
        t.printStackTrace()
        -1
    }
}
