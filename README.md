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

If *transformDebugClassesWithAsm* can be found in the output, it means *Booster* is enabled. Congratulations! 🎉🎉🎉

> 如果在命令行输出中能搜到 *transformDebugClassesWithAsm* 说明 *Booster* 已经启用了，那么恭喜你！ 🎉🎉🎉

Please note that *Booster* only registers the class transformation when at least one `booster-transform-*` module (or a custom `Transformer`) is on the buildscript classpath, otherwise nothing is registered and the build is not affected at all.

> 请注意，只有当 buildscript classpath 中至少存在一个 `booster-transform-*` 模块（或自定义的 `Transformer`）时，*Booster* 才会注册字节码转换，否则不会注册任何转换，也不会对构建产生任何影响。


The `plugins` DSL also supported since Booster *3.0.0*

> *Booster* 从 *3.0.0* 开始支持 `plugins` *DSL* 的方式来启用

```groovy
plugins {
    id 'com.didiglobal.booster' version '5.1.0'
}
```

## Transform Mode | 转换模式

Since *5.3.0*, *Booster* has two modes to transform classes, and chooses one automatically by the [`TransformScope`](./booster-transform-spi/src/main/kotlin/com/didiglobal/booster/transform/TransformScope.kt) which transformers declare:

- `instrumentation`: transforms each class independently with [AGP instrumentation](https://developer.android.com/reference/tools/gradle-api/current/com/android/build/api/variant/Instrumentation), the boundaries of inputs (project classes, subprojects and external libraries) are preserved, so that only the changed classes and jars are re-transformed and re-dexed in incremental builds. Chosen when all transformers declare `TransformScope.CLASS`, which is the case for all the built-in `booster-transform-*` modules.
- `scoped-artifacts`: merges all classes into a single jar, the transformers have the whole program at hand (full classpath, `Collector`, all artifacts), but a change of a single class causes the whole jar to be re-dexed, see [#498](https://github.com/didi/booster/issues/498). Chosen when any transformer declares `TransformScope.PROGRAM` (the default of the SPI, and always the case for `JavassistTransformer`).

> 从 *5.3.0* 开始，*Booster* 有两种转换模式，并根据 Transformer 声明的 [`TransformScope`](./booster-transform-spi/src/main/kotlin/com/didiglobal/booster/transform/TransformScope.kt) 自动选择：
>
> - `instrumentation`：通过 [AGP instrumentation](https://developer.android.com/reference/tools/gradle-api/current/com/android/build/api/variant/Instrumentation) 逐个转换 class，输入的边界（工程 class、子工程和外部依赖）得以保留，因此增量构建时只有变化的 class 和 jar 会被重新转换和 DEX。当所有 Transformer 都声明 `TransformScope.CLASS` 时选用，内置的 `booster-transform-*` 模块均满足。
> - `scoped-artifacts`：将所有 class 聚合为单个 jar，Transformer 拥有全程序视图（完整 classpath、`Collector`、所有产物），但单个 class 的变化会导致整个 jar 重新 DEX，详见 [#498](https://github.com/didi/booster/issues/498)。当任一 Transformer 声明 `TransformScope.PROGRAM`（SPI 的默认值，`JavassistTransformer` 始终如此）时选用。

Custom transformers which transform each class independently should declare `TransformScope.CLASS` to keep incremental builds fast:

> 逐个 class 独立转换的自定义 Transformer 应当声明 `TransformScope.CLASS` 以保持增量构建的速度：

```kotlin
@AutoService(ClassTransformer::class)
class MyTransformer : ClassTransformer {
    override val scope = TransformScope.CLASS
    ...
}
```

The finalized scope and the transformers which require the whole program are printed at the beginning of the build.

> 最终的 scope 以及要求全程序视图的 Transformer 会在构建开始时打印出来。

The differences between the two modes | 两种模式的差异：

| | `instrumentation` | `scoped-artifacts` |
|---|---|---|
| Incremental build | only changed classes/jars are re-transformed and re-dexed | the whole merged jar is re-dexed |
| `TransformContext.klassPool` | backed by AGP class hierarchy | backed by `URLClassLoader` |
| `TransformContext.artifacts` | `MERGED_MANIFESTS`, `SYMBOL_LIST` only | all artifacts |
| `TransformContext.dependencies` | identifiers of runtime dependencies, e.g. `androidx.core:core:1.12.0` | paths of input jars |
| `TransformContext.compileClasspath` / `runtimeClasspath` | empty | input jars and directories |
| `Collector` | not supported | supported |
| `Transformer.onPostTransform` | called once when the build finishes | called when the transform task finishes |

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
