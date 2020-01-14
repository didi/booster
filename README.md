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

  Potential performance issues could be found by using Booster, for example, calling APIs that may block the UI thread or main thread, such as I/O APIs. About the details, please see [booster-transform-lint](./booster-transform-lint).

  > 使用 Booster 可以发现潜在的性能问题，例如，在应用中调用可能阻塞 UI 线程或者主线程的 API，如：I/O API 等。详情请参见： [booster-transform-lint](./booster-transform-lint)。

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

- Gradle version 4.1+
- Android Gradle Plugin version 3.0+ (3.2.0+ is recommended)

## Best Practise | 最佳实践

The best practise of using Booster is integrating the specific moudle to solve the problems you have encountered as following:

> 集成 Booster 的最佳方式是集成真正需要的模块来解决项目中遇到的特定问题。

```groovy
buildscript {
    ext.booster_version = '1.2.1'
    repositories {
        google()
        mavenCentral()
        jcenter()
        maven { url 'https://oss.sonatype.org/content/repositories/public' }
    }
    dependencies {
        classpath "com.didiglobal.booster:booster-gradle-plugin:$booster_version"
        // figure out the features you really need, then choose the right module for integration
        // 弄清楚真正需要的特性，然后从下面的模块列表中选择正确的模块进行集成
    }
}

allprojects {
    repositories {
        google()
        mavenCentral()
        jcenter()
        maven { url 'https://oss.sonatype.org/content/repositories/public' }
    }
}
```

Here are all the modules of Booster:

### Performance

- [booster-transform-lint](./booster-transform-lint) - 性能瓶颈检测

  ```groovy
  classpath "com.didiglobal.booster:booster-transform-lint:$booster_version"
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
  classpath "com.didiglobal.booster:booster-task-compression-pngquant:$booster_version"
  ```

- [booster-task-compression-pngquant](./booster-task-compression-pngquant) - 采用 pngquant 对资源进行压缩

  ```groovy
  classpath "com.didiglobal.booster:booster-task-compression-pngquant:$booster_version"
  ```

- [booster-task-compression-processed-res](./booster-task-compression-processed-res) - ap_ 文件压缩

  ```groovy
  classpath "com.didiglobal.booster:booster-task-compression-processed-res:$booster_version"
  ```

- [booster-task-resource-deredundancy](./booster-task-resource-deredundancy) - 去冗余资源

  ```groovy
  classpath "com.didiglobal.booster:booster-task-resource-deredundancy:$booster_version"
  ```

- [booster-transform-r-inline](./booster-transform-r-inline) - 资源索引内联

  ```groovy
  classpath "com.didiglobal.booster:booster-transform-r-inline:$booster_version"
  ```

- [booster-transform-br-inline](./booster-transform-br-inline) - DataBinding BR索引内联

  ```groovy
  classpath "com.didiglobal.booster:booster-transform-br-inline:$booster_version"
  ```

- [booster-transform-verifier](./booster-transform-verifier) - Bytecode 校验

  ```groovy
  classpath "com.didiglobal.booster:booster-transform-verifier:$booster_version"
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

About the details, please see [Wiki](../../wiki).

## Contributing

Welcome to contribute by creating issues or sending pull requests. See [Contributing Guideline](./CONTRIBUTING.md).

> 欢迎大家以 issue 或者 pull request 的形式为本项目作贡献。详见 [Contributing Guideline](./CONTRIBUTING.md)。

## Community

Welcome to join the community on [spectrum](https://spectrum.chat/booster).

![Booster交流群](https://user-images.githubusercontent.com/2344882/71557791-269c8e80-2a86-11ea-8272-4c2d12b5219d.png)

## License

Booster is licensed under the [Apache License 2.0](./LICENSE.txt).

