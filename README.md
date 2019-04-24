# Booster

![GitHub](https://img.shields.io/github/license/didi/booster.svg?style=for-the-badge)
![Travis (.org)](https://img.shields.io/travis/didi/booster.svg?style=for-the-badge)
![GitHub contributors](https://img.shields.io/github/release/didi/booster.svg?style=for-the-badge)

Booster is an easy-to-use, lightweight, powerful and extensible quality optimization toolkit designed specially for mobile applications. Using the dynamic discovering and loading mechanism, booster provides the ability for customizing. In other words, Booster is an quality optimization framework for mobile applications

Booster 是专门为移动应用而设计的简单易用、轻量级、功能强大且可扩展的质量优化工具包，其通过动态发现和加载机制提供可扩展的能力，换言之，Booster 也是一个移动应用质量优化框架。

## Prerequisite | 先决条件

- Gradle version 4.1+ | Gradle 4.1 以上版本
- Android Gradle Plugin version 3.0+ | Android Gradle 插件 3.0 以上版本

## Getting Started | 快速上手

The plugin can be added to the buildscript classpath and applied:

在 `buildscript` 的 classpath 中引入 Booster 插件，然后启用该插件：

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

Booster 是一个模块化的工程，其优化器由 Gradle 插件和一系列 Transformer 组成，这意味着，至少需要显式地依赖一个 transformer 才能得到预期的效果。为了方便起见，可以通过依赖 `booster-transform-all` 模块来启用所有的优化项。

In addition, Booster provides a collection of [Gradle Task](https://docs.gradle.org/current/userguide/tutorial_using_tasks.html) to help developers be more efficient.
Conveniently, the `booster-task-all` module can be depended to enable all tasks.

另外，Booster 也提供了一系列的 [Gradle Task](https://docs.gradle.org/current/userguide/tutorial_using_tasks.html) 来帮助开发者提升效率，为了方便起见，可以通过依赖 `booster-task-all` 来启用所有的 task。

Then build an optimized package by executing the *assemble* task:

然后通过执行 `assemble` task 来构建一个优化过的应用包：

```bash
$ ./gradlew assembleRelease
```

# Documentation | 文档

About the details, please see [Wiki](./wiki)

详见 [Wiki](./wiki)。

# Contributing

Welcome to contribute by creating issues or sending pull requests. See [Contributing Guideline](./CONTRIBUTING.md)

欢迎大家以 issue 或者 pull request 的形式为本项本作贡献。详见 [Contributing Guideline](./CONTRIBUTING.md)

# License

Booster is licensed under the [Apache License 2.0](./LICENSE.txt).

