# Booster

![GitHub](https://img.shields.io/github/license/didi/booster.svg?style=for-the-badge)
![Travis (.org)](https://img.shields.io/travis/didi/booster.svg?style=for-the-badge)
![GitHub contributors](https://img.shields.io/github/release/didi/booster.svg?style=for-the-badge)

## Overview | 概览

Booster is an easy-to-use, lightweight, powerful and extensible quality optimization toolkit designed specially for mobile applications. Using the dynamic discovering and loading mechanism, Booster provides the ability for customizing. In other words, Booster is a quality optimization framework for mobile applications.

Booster 是专门为移动应用而设计的简单易用、轻量级、功能强大且可扩展的质量优化工具包，其通过动态发现和加载机制提供可扩展的能力，换言之，Booster 也是一个移动应用质量优化框架。

Booster consists chiefly of transformers and tasks, transformers are used for byte code scanning or manipulation (depends on the transformer's functionalities), tasks are used for artifact processing, to satisfy specialized optimization requirements, Booster provides [Transformer SPI](../tree/master/booster-transform-spi) and [VariantProcessor SPI](../tree/master/booster-task-spi) for developers to support customization. The following figure shows the architecture of Booster:

Booster 主要由 Transformer 和 Task 组成，Transformer 主要用于对字节码进行扫描或修改（取决于 Transformer 的功能），Task 主要用于构建过程中的资源处理，为了满足特异的优化需求，Booster 提供了 [Transformer SPI](../tree/master/booster-transform-spi) and [VariantProcessor SPI](../tree/master/booster-task-spi) 允许开发者进行定制，以下是 Booster 的整体框架：

![Booster Architecture](https://github.com/didichuxing/booster/raw/master/assets/booster-architecture.png)

## Features | 特性

- Performance detection | 性能检测
- Performance optimzation | 性能优化
- Package size reduction | 包体积瘦身
- Code instrumentation | 代码注入

### Built-in Transformers | 内置 Transformers

- [booster-transform-bugfix-toast](../tree/master/booster-transform-bugfix-toast)

    Used to fix system bug caused by Toast on Android 7.1.1 (N MR1)

    用于修复 Android 7.1.1 (N MR1) 中 Toast 导致的系统错误

- [booster-transform-lint](../tree/master/booster-transform-lint)

    Used for potential performance issue detecting

    用于检测潜在的性能问题

- [booster-transform-shrink](../tree/master/booster-transform-shrink)

    Used for constants shrinking in class file

    用于清除 class 文件中的常量（如：BuildConfig.class、R$id.class、R$layout.class 等）

- [booster-transform-usage](../tree/master/booster-transform-usage)

    Used for API usage searching

    用于扫描特定 API 的使用情况

### Built-in Tasks | 内置 Tasks

- [booster-task-artifact](../tree/master/booster-task-artifact)

    Provides tasks show all artifacts

    提供显示 artifact 的 task

- [booster-task-dependency](../tree/master/booster-task-dependency)

    Provides tasks to show the module identifier and file path of each dependency

    提供显示所有依赖项的标识符及文件路径的 task

- [booster-task-permission](../tree/master/booster-task-permission)

    Provides tasks to show Android permission usage of each dependency

    提供显示所有依赖项使用的 Android 权限的 task

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

## Documentation | 文档

About the details, please see [Wiki](../../wiki)

详见 [Wiki](../../wiki)。

## Contributing

Welcome to contribute by creating issues or sending pull requests. See [Contributing Guideline](./CONTRIBUTING.md)

欢迎大家以 issue 或者 pull request 的形式为本项本作贡献。详见 [Contributing Guideline](./CONTRIBUTING.md)

## License

Booster is licensed under the [Apache License 2.0](./LICENSE.txt).

