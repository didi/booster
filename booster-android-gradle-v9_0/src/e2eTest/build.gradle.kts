buildscript {
    dependencies {
        classpath("com.didiglobal.booster:booster-transform-thread:1.0.0-SNAPSHOT")
    }
}

plugins {
    id("com.android.application") version "9.0.1" apply false
    id("com.didiglobal.booster") version "1.0.0-SNAPSHOT" apply false
}
