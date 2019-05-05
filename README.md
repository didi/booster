![Booster](assets/booster-logo.png)

![GitHub](https://img.shields.io/github/license/didi/booster.svg?style=for-the-badge)
![Travis (.org)](https://img.shields.io/travis/didi/booster.svg?style=for-the-badge)
![GitHub release](https://img.shields.io/github/release/didi/booster.svg?style=for-the-badge)

## Overview | 概览

Booster is an easy-to-use, lightweight, powerful and extensible quality optimization toolkit designed specially for mobile applications. Using the dynamic discovering and loading mechanism, Booster provides the ability for customizing. In other words, Booster is a quality optimization framework for mobile applications.

Booster 是专门为移动应用而设计的简单易用、轻量级、功能强大且可扩展的质量优化工具包，其通过动态发现和加载机制提供可扩展的能力，换言之，Booster 也是一个移动应用质量优化框架。

Booster consists chiefly of transformers and tasks, transformers are used for byte code scanning or manipulation (depends on the transformer's functionalities), tasks are used for artifact processing, to satisfy specialized optimization requirements, Booster provides [Transformer SPI](./booster-transform-spi) and [VariantProcessor SPI](./booster-task-spi) for developers to support customization. The following figure shows the architecture of Booster:

Booster 主要由 Transformer 和 Task 组成，Transformer 主要用于对字节码进行扫描或修改（取决于 Transformer 的功能），Task 主要用于构建过程中的资源处理，为了满足特异的优化需求，Booster 提供了 [Transformer SPI](./booster-transform-spi) and [VariantProcessor SPI](./booster-task-spi) 允许开发者进行定制，以下是 Booster 的整体框架：

![Booster Architecture](https://github.com/didichuxing/booster/raw/master/assets/booster-architecture.png)

## What can Booster be used for? | Booster 能做什么？

- Detecting performance issues | 检测性能问题

  Potential performance issues could be found by using Booster, for example, calling APIs that may block the UI thread or main thread, such as I/O APIs.

  使用 Booster 可以发现潜在的性能问题，例如，在应用中调用可能阻塞 UI 线程或者主线程的 API，如：I/O API。

- Optimizing runtime performance | 优化运行时性能

  Thread management has always been a problem for developers, especially the threads started by third-party SDKs, starting too many threads may cause OOM, fortunately, these issues can be solved by Booster.

  对于开发者来说，线程管理一直是个头疼的问题，特别是第三方 SDK 中的线程，过多的线程可能会导致内存不足，然而幸运的是，这些问题都能通过 Booster 来解决。

- Fixing system bugs | 修复系统错误

  Such as fixing the crash caused by `Toast` globally on [Android API 25](https://developer.android.com/studio/releases/platforms#7.1).

  例如全局性地修复 [Android API 25](https://developer.android.com/studio/releases/platforms#7.1) 版本中 `Toast` 导致的崩溃。

- Reducing app size | 为应用瘦身

  Such as image resources compression, constants removal, etc.

  像图片资源压缩、代码中常量的删除，都可以通过 Booster 来完成

- Other things you can imagine | 其它你能想像得到的

## Prerequisite | 先决条件

- Gradle version 4.1+ | Gradle 4.1 以上版本
- Android Gradle Plugin version 3.0+ | Android Gradle 插件 3.0 以上版本

## Getting Started | 快速上手

The plugin can be added to the buildscript classpath and applied:

在 `buildscript` 的 classpath 中引入 Booster 插件，然后启用该插件：

```groovy
buildscript {
    ext.booster_version = '0.1.6'
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

Then build an optimized package by executing the *assemble* task, after the build process completed, the reports could be found at `build/reports/`

然后通过执行 `assemble` task 来构建一个优化过的应用包，构建完成后，在 `build/reports/` 目录下会生成相应的报告

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

