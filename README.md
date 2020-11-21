![Booster](assets/booster-logo.png)

![GitHub](https://img.shields.io/github/license/didi/booster.svg?style=for-the-badge)
![Travis (.org)](https://img.shields.io/travis/didi/booster.svg?style=for-the-badge)
![GitHub release](https://img.shields.io/github/release/didi/booster.svg?style=for-the-badge)

## Overview | 概览

Booster is an easy-to-use, lightweight, powerful and extensible quality optimization toolkit designed specially for mobile applications. The primary goal is to solve quality problems with the increase of APP complexity, such as performance, stability, and package size, etc.

Booster provides a collection of modules for performance detection, multithreading optimization, resources index inline, redundant resources reduction, resources compression, system bug fixing, etc. Using booster, the stability of application can be increased by 15% ~ 25%, and the package size can be reduced by 1MB ~ 10MB.

>  Booster 是一款专门为移动应用设计的易用、轻量级且可扩展的质量优化框架，其目标主要是为了解决随着 APP 复杂度的提升而带来的性能、稳定性、包体积等一系列质量问题。
>
> Booster 提供了性能检测、多线程优化、资源索引内联、资源去冗余、资源压缩、系统 Bug 修复等一系列功能模块，可以使得稳定性能够提升 15% ~ 25%，包体积可以减小 1MB ~ 10MB。

## What can Booster be used for? | Booster 能做什么？

- Performance detection | 性能检测

  Potential performance issues could be found by using Booster, for example, calling APIs that may block the UI thread or main thread, such as I/O APIs. About the details, please see [booster-task-analyser](./booster-task-analyser).

  > 使用 Booster 可以发现潜在的性能问题，例如，在应用中调用可能阻塞 UI 线程或者主线程的 API，如：I/O API 等。详情请参见： [booster-task-analyser](./booster-task-analyser)。

- Performance optimization | 性能优化

  Thread management has always been a problem for developers, especially the threads started by third-party SDKs, starting too many threads may cause OOM, fortunately, these issues can be solved by Booster. About the details, please see [booster-transform-thread](./booster-transform-thread)。

  > 对于开发者来说，线程管理一直是个头疼的问题，特别是第三方 SDK 中的线程，过多的线程可能会导致内存不足，然而幸运的是，这些问题都能通过 Booster 来解决。

- System bugs fix | 系统问题修复

  Such as fixing the crash caused by `Toast` globally on [Android API 25](https://developer.android.com/studio/releases/platforms#7.1). About the details, please see [booster-transform-toast](./booster-transform-toast) and [booster-transform-shared-preferences](./booster-transform-shared-preferences).

  > 例如全局性地修复 [Android API 25](https://developer.android.com/studio/releases/platforms#7.1) 版本中 `Toast` 导致的崩溃。详情请参见：[booster-transform-toast](./booster-transform-toast)、[booster-transform-shared-preferences](./booster-transform-shared-preferences).

- Package size reduction | 应用瘦身

  Such as [image resources compression](./booster-task-compression), [r inline](./booster-transform-r-inline), etc.

  > 如：[资源压缩及冗余资源删除](./booster-task-compression)、[资源索引内联](./booster-transform-r-inline)。

- Other things you can imagine | 其它你能想像得到的

## Prerequisite | 先决条件

- JDK (`1.8` is recommended)
- Gradle version `4.1+`
- Android Gradle Plugin version `3.0+`

The following table lists which version of Gradle is required for each version of the Android Gradle plugin. For the best performance, please use the latest possible version of both Gradle and the plugin.

> 下表列出了各个 Android Gradle 插件版本所需的 Gradle 版本。要获得最佳性能，请使用 Gradle 和插件这两者的最新版本。

| Android Gradle Plugin |  Gradle  |
|:---------------------:|:--------:|
| 3.0.0+                | 4.1+     |
| 3.1.0+                | 4.4+     |
| 3.2.0 - 3.2.1         | 4.6+     |
| 3.3.0 - 3.3.3         | 4.10.1+  |
| 3.4.0 - 3.4.3         | 5.1.1+   |
| 3.5.0 - 3.5.4         | 5.4.1+   |
| 3.6.0 - 3.6.4         | 5.6.4+   |
| 4.0.0+                | 6.1.1+   |
| 4.1.0+                | 6.5+     |

## Best Practise | 最佳实践

The best practise of using Booster is integrating the specific module to solve the problems you have encountered as following:

> 集成 Booster 的最佳方式是集成真正需要的模块来解决项目中遇到的特定问题。

```groovy
buildscript {
    ext.booster_version = '3.0.0'
    repositories {
        google()
        mavenCentral()
        jcenter()

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
        jcenter()

        // OPTIONAL If you want to use SNAPSHOT version, sonatype repository is required.
        maven { url 'https://oss.sonatype.org/content/repositories/public' }
    }
}

apply plugin: 'com.android.application'
apply plugin: 'com.didiglobal.booster' // ③
```

The `plugins` DSL also supported since Booster 3.0.0

Here are all the modules of Booster:

### Common

- [booster-aapt2](./booster-aapt2) - AAPT2 相关 API

  ```groovy
  implementation "com.didiglobal.booster:booster-aapt2:$booster_version"
  ```

- [booster-api](./booster-api) - Booster 插件开发 API

  this module contains both [booster-transform-spi](./booster-transform-spi) and [booster-task-spi](./booster-task-spi)

  ```groovy
  implementation "com.didiglobal.booster:booster-api:$booster_version"
  ```

- [booster-cha](./booster-cha) - Class Hierarchy Analysis API

  ```groovy
  implementation "com.didiglobal.booster:booster-cha:$booster_version"
  ```

- [booster-command](./booster-command) - SPI for external command discovery

  ```groovy
  implementation "com.didiglobal.booster:booster-command:$booster_version"
  ```

### Performance

- [booster-task-analyser](./booster-task-analyser) - 静态分析工具

  ```groovy
  classpath "com.didiglobal.booster:booster-task-analyser:$booster_version"
  ```

- [booster-transform-thread](./booster-transform-thread) - 多线程优化

  ```groovy
  classpath "com.didiglobal.booster:booster-transform-thread:$booster_version"
  ```

- [booster-transform-webview](./booster-transform-webview) - WebView 预加载

  ```groovy
  classpath "com.didiglobal.booster:booster-transform-webview:$booster_version"
  ```

- [booster-transform-shared-preferences](./booster-transform-shared-preferences) - `SharedPreferences` 优化

  ```groovy
  classpath "com.didiglobal.booster:booster-transform-shared-preferences:$booster_version"
  ```

### Package Size

- [booster-task-compression-cwebp](./booster-task-compression-cwebp) - 采用 cwebp 对资源进行压缩

  ```groovy
  classpath "com.didiglobal.booster:booster-task-compression-cwebp:$booster_version"
  ```

  The option `android.precompileDependenciesResources` need to be set on Android Gradle Plugin 3.6 and higher

  ```properties
  android.precompileDependenciesResources=false
  ```

- [booster-task-compression-pngquant](./booster-task-compression-pngquant) - 采用 pngquant 对资源进行压缩

  ```groovy
  classpath "com.didiglobal.booster:booster-task-compression-pngquant:$booster_version"
  ```

  The option `android.precompileDependenciesResources` need to be set on Android Gradle Plugin 3.6 and higher

  ```properties
  android.precompileDependenciesResources=false
  ```

  > [booster-pngquant-provider](https://github.com/johnsonlee/booster-pngquant-provider) could be used for image compressing by *pngquant* without installation

- [booster-task-compression-processed-res](./booster-task-compression-processed-res) - ap_ 文件压缩

  ```groovy
  classpath "com.didiglobal.booster:booster-task-compression-processed-res:$booster_version"
  ```

- [booster-task-resource-deredundancy](./booster-task-resource-deredundancy) - 去冗余资源

  ```groovy
  classpath "com.didiglobal.booster:booster-task-resource-deredundancy:$booster_version"
  ```

  The option `android.precompileDependenciesResources` need to be set on Android Gradle Plugin 3.6 and higher

  ```properties
  android.precompileDependenciesResources=false
  ```

- [booster-transform-r-inline](./booster-transform-r-inline) - 资源索引内联

  ```groovy
  classpath "com.didiglobal.booster:booster-transform-r-inline:$booster_version"
  ```

- [booster-transform-br-inline](./booster-transform-br-inline) - DataBinding BR索引内联

  ```groovy
  classpath "com.didiglobal.booster:booster-transform-br-inline:$booster_version"
  ```

### System Bug

- [booster-transform-finalizer-watchdog-daemon](./booster-transform-finalizer-watchdog-daemon) - 修复 *finalizer* 导致的 `TimeoutException`

  ```groovy
  classpath "com.didiglobal.booster:booster-transform-finalizer-watchdog-daemon:$booster_version"
  ```

- [booster-transform-media-player](./booster-transform-media-player) - 修复 MediaPlayer 崩溃

  ```groovy
  classpath "com.didiglobal.booster:booster-transform-media-player:$booster_version"
  ```

- [booster-transform-res-check](./booster-transform-res-check) - 检查覆盖安装导致的 *Resources* 和 *Assets* 未加载的 Bug

  ```groovy
  classpath "com.didiglobal.booster:booster-transform-res-check:$booster_version"
  ```

- [booster-transform-toast](./booster-transform-toast) - 修复 Toast 在 Android 7.1 上的 Bug

  ```groovy
  classpath "com.didiglobal.booster:booster-transform-toast:$booster_version"
  ```

- [booster-transform-activity-thread](./booster-transform-activity-thread) - 处理系统 Crash

  ```groovy
  classpath "com.didiglobal.booster:booster-transform-activity-thread:$booster_version"
  ```

### Utility

- [booster-task-check-snapshot](./booster-task-check-snapshot) - 检查 SNAPSHOT 版本

  ```groovy
  classpath "com.didiglobal.booster:booster-task-check-snapshot:$booster_version"
  ```

- [booster-task-list-permission](./booster-task-list-permission) - 显示 AAR 使用的权限清单

  ```groovy
  classpath "com.didiglobal.booster:booster-task-list-permission:$booster_version"
  ```

- [booster-task-list-shared-library](./booster-task-list-shared-library) - 显示 AAR 包含的动态库清单

  ```groovy
  classpath "com.didiglobal.booster:booster-task-list-shared-library:$booster_version"
  ```

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

