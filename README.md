# Booster

![GitHub](https://img.shields.io/github/license/didi/booster.svg?style=for-the-badge)
![Travis (.org)](https://img.shields.io/travis/didi/booster.svg?style=for-the-badge)
![GitHub contributors](https://img.shields.io/github/release/didi/booster.svg?style=for-the-badge)

Booster is an easy-to-use, lightweight, powerful and extensible optimization toolkit designed specially for mobile applications. Using the dynamic discovering and loading mechanism, booster provides the ability for customizing.

## Prerequisite

- Gradle version 4.1+
- Android Gradle Plugin version 3.0+

## Getting Started

The plugin can be added to the buildscript classpath and applied:

```groovy
buildscript {
    ext.booster_version = '0.1.0'
    repositories {
        google()
        mavenCentral()
        jcenter()
    }
    dependencies {
        classpath "com.didiglobal.booster:booster-gradle-plugin:$booster_version"
        classpath "com.didiglobal.booster:booster-task-all:$booster_version"
        classpath "com.didiglobal.booster:booster-transform-all:$booster_version"
    }
}

apply plugin: 'com.android.application'
apply plugin: 'com.didiglobal.booster'
```

Booster is a modularized project, and the optimizer consist of gradle plugin and a dozen of transformers.
This means, at least one transformer must be *explicitly* depended to have the desired effect.
Conveniently, the `booster-transform-all` module can be depended to enable all optimization options.

In addition, Booster provides a collection of [Gradle Task](https://docs.gradle.org/current/userguide/tutorial_using_tasks.html) to help developers be more efficient.
Conveniently, the `booster-task-all` module can be depended to enable all tasks.

Then build an optimized package by executing the *assemble* task:

```bash
$ ./gradlew assembleRelease
```

# Contributing

Welcome to contribute by creating issues or sending pull requests. See [Contributing Guideline](./CONTRIBUTING.md)

# License

Booster is licensed under the [Apache License 2.0](./LICENSE.txt).
