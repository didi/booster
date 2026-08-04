![Booster](assets/booster-logo.png)

![GitHub](https://img.shields.io/github/license/didi/booster.svg?style=for-the-badge)
![GitHub Release](https://img.shields.io/github/release/didi/booster.svg?style=for-the-badge)

## Overview | 概览

Booster is an easy-to-use, lightweight, powerful and extensible quality optimization toolkit designed specially for mobile applications. The primary goal is to solve quality problems with the increase of APP complexity, such as performance, stability, and package size, etc.

Booster provides a collection of modules for performance detection, multithreading optimization, resources index inline, redundant resources reduction, resources compression, system bug fixing, etc. Using booster, the stability of application can be increased by 15% ~ 25%, and the package size can be reduced by 1MB ~ 10MB.

>  Booster 是一款专门为移动应用设计的易用、轻量级且可扩展的质量优化框架，其目标主要是为了解决随着 APP 复杂度的提升而带来的性能、稳定性、包体积等一系列质量问题。
>
> Booster 提供了性能检测、多线程优化、资源索引内联、资源去冗余、资源压缩、系统 Bug 修复等一系列功能模块，可以使得稳定性能够提升 15% ~ 25%，包体积可以减小 1MB ~ 10MB。

## What can Booster be used for? | Booster 能做什么？

- Performance detection | 性能检测

  Potential performance issues could be found by using Booster, for example, calling APIs that may block the UI thread or main thread, such as I/O APIs. About the details

  > 使用 Booster 可以发现潜在的性能问题，例如，在应用中调用可能阻塞 UI 线程或者主线程的 API，如：I/O API 等。

- Performance optimization | 性能优化

  Thread management has always been a problem for developers, especially the threads started by third-party SDKs, starting too many threads may cause OOM, fortunately, these issues can be solved by Booster. About the details, please see [booster-transform-thread](./booster-transform-thread)。

  > 对于开发者来说，线程管理一直是个头疼的问题，特别是第三方 SDK 中的线程，过多的线程可能会导致内存不足，然而幸运的是，这些问题都能通过 Booster 来解决。

- System bugs fix | 系统问题修复

  Such as fixing the crash caused by `Toast` globally on [Android API 25](https://developer.android.com/studio/releases/platforms#7.1). About the details, please see [booster-transform-toast](./booster-transform-toast).

  > 例如全局性地修复 [Android API 25](https://developer.android.com/studio/releases/platforms#7.1) 版本中 `Toast` 导致的崩溃。详情请参见：[booster-transform-toast](./booster-transform-toast).

- Package size reduction | 应用瘦身

  Such as [r inline](./booster-transform-r-inline), etc.

  > 如：[资源索引内联](./booster-transform-r-inline)。

- Other things you can imagine | 其它你能想像得到的

## Prerequisite | 先决条件

- JDK (minimum version required is `JDK 1.8`, `JDK 11` is recommended)
- Gradle version `4.10+`
- Android Gradle Plugin version `3.3+`

The following table lists which version of Gradle is required for each version of the Android Gradle plugin. For the best performance, please use the latest possible version of both Gradle and the plugin.

> 下表列出了各个 Android Gradle 插件版本所需的 Gradle 版本。要获得最佳性能，请使用 Gradle 和插件这两者的最新版本。

| Android Gradle Plugin | Gradle  | Booster |
|:---------------------:|:-------:|:-------:|
|         9.0.x         |  9.1+   | 5.2.0+  |
|         8.12          |  8.13+  | 5.2.0+  |
|        8.9-8.11       | 8.11.1+ | 5.2.0+  |
|          8.8          | 8.10.2+ | 5.2.0+  |
|          8.7          |  8.9+   | 5.2.0+  |
|          8.6          |  8.7+   | 5.2.0+  |
|          8.5          |  8.7+   | 5.2.0+  |
|          8.4          |  8.6+   | 5.2.0+  |
|          8.3          |  8.4+   | 5.2.0+  |
|          8.2          |  8.2+   | 5.0.0+  |
|          8.1          |  8.0+   | 5.0.0+  |
|          8.0          |  8.0+   | 5.0.0+  |
|          7.4          |  7.5+   | 4.16.3+ |
|          7.3          |  7.4+   | 4.15.0+ |
|          7.2          | 7.3.3+  | 4.10.0+ |
|          7.1          |  7.1+   | 4.10.0+ |
|          7.0          |  7.0+   | 4.0.0+  |
|        4.2.0+         | 6.7.1+  | 3.2.0+  |
|        4.1.0+         |  6.5+   | 3.0.0+  |
|        4.0.0+         | 6.1.1+  | 2.0.0+  |
|     3.6.0 - 3.6.4     | 5.6.4+  | 1.0.0+  |
|     3.5.0 - 3.5.4     | 5.4.1+  | 1.0.0+  |
|     3.4.0 - 3.4.3     | 5.1.1+  | 1.0.0+  |
|     3.3.0 - 3.3.3     | 4.10.1+ | 0.1.0+  |

## Best Practise | 最佳实践

The best practise of using Booster is integrating the specific module to solve the problems you have encountered as following:

> 集成 Booster 的最佳方式是集成真正需要的模块来解决项目中遇到的特定问题。

```groovy
buildscript {
    ext.booster_version = '5.1.0'
    repositories {
        google()
        mavenCentral()

        // OPTIONAL If you want to use SNAPSHOT version, sonatype repository is required.
        maven { url 'https://oss.sonatype.org/content/repositories/public' }
    }
    dependencies {
        classpath "com.didiglobal.booster:booster-gradle-plugin:$booster_version" // ① 
        // ② figure out the features you really need, then choose the right module for integration
        // ② 弄清楚真正需要的特性，然后从下面的模块列表中选择正确的模块进行集成
    }
}

allprojects {
    repositories {
        google()
        mavenCentral()

        // OPTIONAL If you want to use SNAPSHOT version, sonatype repository is required.
        maven { url 'https://oss.sonatype.org/content/repositories/public' }
    }
}

apply plugin: 'com.android.application'
apply plugin: 'com.didiglobal.booster' // ③
```

Then using the following command in terminal to check if Booster enabled

> 然后在终端用如下命令来确认 Booster 是否启用：

```bash
./gradlew assembleDebug --dry-run
```

If *transformClassesWithBoosterForDebug* can be found in the output, it means *Booster* is enabled. Congratulations! 🎉🎉🎉

> 如果在命令行输出中能搜到 *transformClassesWithBoosterForDebug* 说明 *Booster* 已经启用了，那么恭喜你！ 🎉🎉🎉


The `plugins` DSL also supported since Booster *3.0.0*

> *Booster* 从 *3.0.0* 开始支持 `plugins` *DSL* 的方式来启用

```groovy
plugins {
    id 'com.didiglobal.booster' version '5.1.0'
}
```

## Migrate from Booster 4.x to 5.x | 从 Booster 4.x 迁移到 5.x

Due to AGP 8's incompatible changes, AGP 7.x and below are no longer supported, if you are still using AGP 7.x, please use Booster 4.x

> 由于 AGP 8 的不兼容性变更，AGP 7.x 及以下版本已经不再支持，如果你仍在使用 AGP 7.x，请使用 Booster 4.x

Most `Task` based modules are no longer supported in Booster 5.0.0, however, the `Transform` based modules are still supported without breaking changes.

> 大部分基于 `Task` 的模块在 Booster 5.0.0 中已经不再支持，但是基于 `Transform` 的模块仍然支持且没有破坏性变更。

About the details, please see [Migrate from Booster 4.x to 5.x](https://booster.johnsonlee.io/en/migration/)

> 详情请参见 [从 Booster 4.x 迁移到 5.x](https://booster.johnsonlee.io/zh/migration

## Samples | 示例

- [transformer-with-asm](https://github.com/boostersamples/transformer-with-asm)
- [transformer-with-javassist](https://github.com/boostersamples/transformer-with-javassist)

## Documentation | 文档

About the details, please see [Booster Inside（深入理解 Booster）](https://booster.johnsonlee.io)

## API Reference

About the API reference, please see [Booster API Reference](https://reference.johnsonlee.io/booster/)

## Contributing

Welcome to contribute by creating issues or sending pull requests. See [Contributing Guideline](./CONTRIBUTING.md).

> 欢迎大家以 issue 或者 pull request 的形式为本项目作贡献。详见 [Contributing Guideline](./CONTRIBUTING.md)。

## Community

![Booster交流群](https://user-images.githubusercontent.com/2344882/84267118-271e7280-ab58-11ea-8c79-4feced83dd3f.png)

## License

Booster is licensed under the [Apache License 2.0](./LICENSE.txt).
