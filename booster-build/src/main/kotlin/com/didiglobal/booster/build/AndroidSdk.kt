package com.didiglobal.booster.build

import com.didiglobal.booster.kotlinx.OS
import com.didiglobal.booster.kotlinx.file
import java.io.File
import java.io.FileNotFoundException
import java.util.Properties

private val HOME = System.getProperty("user.home")

private val CWD = System.getProperty("user.dir")

/**
 * @author johnsonlee
 */
class AndroidSdk {

    companion object {

        /**
         * Returns the *android.jar* of the specific API level
         *
         * @param apiLevel Android API level
         */
        fun getAndroidJar(apiLevel: Int): File {
            val jar = File(getLocation(), "platforms${File.separator}android-${apiLevel}${File.separator}android.jar")
            return jar.takeIf { it.exists() } ?: throw FileNotFoundException(jar.path)
        }

        /**
         * Returns the Android SDK location, the search order:
         *
         * 1. ANDROID_HOME environment variable
         * 1. android command in PATH
         * 1. local.properties
         * 1. platform dependent path:
         *
         *     - macosx: ~/Library/Android/sdk
         *     - linux: ~/Android/sdk
         *     - windows: ~\AppData\Local\Android\sdk
         */
        fun getLocation(): File = System.getenv("ANDROID_HOME")?.takeIf {
            it.isNotBlank()
        }?.let {
            File(it)
        }?.takeIf {
            it.exists() && it.isDirectory
        } ?: System.getenv("PATH").splitToSequence(File.pathSeparator).map {
            File(it, "android")
        }.find {
            it.exists() && it.canExecute()
        }?.parentFile ?: File(CWD, "local.properties").let { local ->
            if (local.exists()) {
                val props = Properties();
                local.inputStream().use {
                    props.load(it)
                }
                props.getProperty("sdk.dir", null)?.let {
                    File(it)
                }?.takeIf {
                    it.exists() && it.isDirectory
                }
            } else {
                null
            }
        } ?: when {
            OS.isMac() -> File(HOME, "Library${File.separator}Android${File.separator}sdk").takeIf { it.exists() && it.isDirectory }
            OS.isLinux() -> File(HOME, "Android${File.separator}sdk").takeIf { it.exists() && it.isDirectory }
            OS.isWindows() -> File(HOME, "AppData${File.separator}Local${File.separator}Android${File.separator}sdk").takeIf { it.exists() && it.isDirectory }
            else -> null
        } ?: throw RuntimeException("`ANDROID_HOME` is not set and `android` command not in your PATH")
    }

}