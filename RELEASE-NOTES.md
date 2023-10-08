## v4.16.3

- Support AGP 7.4
- Fix issue [#422](https://github.com/didi/booster/pull/422) end-of-stream caused by multiple collector

## v4.14.2

- Fix issue [#422](https://github.com/didi/booster/pull/422) end-of-stream caused by multiple collector

## v4.14.1

- Sync from 4.16.2
- Exclude AGP 7.3 and Kotlin API version 1.5.0 upgrading

## v4.16.2

- Fix `Project.getJarTaskProviders(BaseVariant?)` with projects do not have `android` extension
- Add extension `BaseVariant.localAndroidResources`
- Fix `AndroidSdk.findPlatform()`: Ignore Android SDK extension

## v4.16.1

- Fix issue [#406](https://github.com/didi/booster/issues/406) : incremental build with jar removal
- Fix issue [#407](https://github.com/didi/booster/issues/407) : Apple M1 chipset support for cwebp

## v4.15.0

- Fix issue [#381](https://github.com/didi/booster/issues/381)
- Fix issue [#387](https://github.com/didi/booster/issues/387)
- Upgrade Kotlin API version to 1.5.0
- Support AGP 7.3
- Fix integration tests

- CHA improvement

## v4.13.0

- Fix incompatibility issue of `ShadowScheduledThreadPoolExecutor` on Android 5.1.1 and below
- Fix issue [#364](https://github.com/didi/booster/issues/364) booster transform cannot be registered successfully
- Fix issue [#368](https://github.com/didi/booster/issues/368) by removing JAR signature related files to prevent JAR signature verification
- Fix issue [#370](https://github.com/didi/booster/issues/370) by improving the compatibility of AGP 7.2.0+
- Improve [booster-aapt2](https://github.com/didi/booster/blob/v4.13.0/booster-aapt2) compatibility

## v4.12.0

- Enable class set cache for static analysis to reduce memory footprint
- Support Gradle configuration cache [#248](https://github.com/didi/booster/issues/248)
- Loading and initializing variant processor earlier

## v4.11.0

- Grouping tasks into group `booster`
- Dependency acquisition enhancement
- Fix transform timing issue
- Add [booster-cha-asm](https://github.com/didi/booster/blob/v4.11.0/booster-cha-asm) to support ASM based CHA
- Improvement for [booster-task-analyser](https://github.com/didi/booster/blob/v4.11.0/booster-task-analyser)
- Load all classes of a composite class set before accessing
- Fix bug of resolving project dependencies

## v4.10.0

- Improve ClassSet to avoid memory leaks
- Improve ClassSet to support loading classes from AAR
- Fix transform output conflicts
- Add `execute(...)` for `Command`
- Add class reference analysis for [booster-task-analyser](https://github.com/didi/booster/blob/v4.10.0/booster-task-analyser)
- Splitting [booster-graph](https://github.com/didi/booster/blob/v4.10.0/booster-graph) into [booster-graph](https://github.com/didi/booster/blob/v4.10.0/booster-graph), [booster-graph-dot](https://github.com/didi/booster/blob/v4.10.0/booster-graph-dot) and [booster-graph-json](https://github.com/didi/booster/blob/v4.10.0/booster-graph-json)
- AGP 7.1 & 7.2 compatibility support

## v4.9.0

- Fix [CVE-2020-15250] In JUnit4 from version 4.7 and before 4.13.1, the test rule TemporaryFolder contains a local information disclosure vulnerability
- Fix variant artifacts acquiring
- Fix integration tests
- Refactoring [booster-graph](https://github.com/didi/booster/blob/v4.9.0/booster-graph) to support grouping and render options

## v4.8.0

- Fix issue [#311](https://github.com/didi/booster/issues/311)
- Add api `mergeNativeLibsTaskProvider`
- Add [booster-graph](https://github.com/didi/booster/blob/v4.8.0/booster-graph) to generate graph with `dot`
- Using deferred task instead of creating task directly
- Reverse the edge direction of the task graph

## v4.7.0

- Add `DotGraph.visualize()` for graph visualization
- Add [booster-task-graph](https://github.com/didi/booster/blob/v4.7.0/booster-task-graph) for task graph visualization
- Fix improper task dependencies
- Fix issue [#304](https://github.com/didi/booster/issues/304): `ScheduledThreadPoolExecutor` transform bug

## v4.6.0

- Add extension property `BaseVariant.mergedNativeLibs: Collection<File>`
- Add extension function `BaseVariant..getReport(String, String): File`
- Fix Jacoco coverage report issue

## v4.5.3

- Fix `mergeResources` task

## v4.5.2

- Fix issue [#284](https://github.com/didi/booster/issues/284)

## v4.5.1

- Fix issue [#287](https://github.com/didi/booster/issues/287)

## v4.5.0

- Fix issue [#280](https://github.com/didi/booster/issues/280)

## v4.4.0

- Support force update inputs for incremental build

## v4.3.0

- Support running local unit test with transformer
- Add collector API for multi-round transform

## v4.2.0

- Add Android stub APIs
- Fix bug of the classpath of class pool
- Add runtime instrumentation support

## v4.1.0

- Fix issue [#258](https://github.com/didi/booster/issues/258)
- Fix bug when install in android-R machine: no compress arsc file.

## v4.0.0

- Support AGP 7.0, no longer supported AGP 3.2 and lower versions

## v3.5.0

- Fix `booster-transform-r-inline` 支持 `constraintlayout` `v2.0`
- Add `booster-transform-service-loader` for `ServiceLoader` performance optimization

## v3.4.0

- Refactor `booster-cha`，to support bytecode manipulation framework independent `ClassParser`
- Update `auto-service` version to `1.0`
- Update `common-compression` version to `1.21`

## v3.3.1

- Fix issue [#222](https://github.com/didi/booster/issues/222)
- Fix issue [#224](https://github.com/didi/booster/issues/224) : Context.getFilesDir() returns null
- Fix issue [#231](https://github.com/didi/booster/issues/231)

## v3.3.0

- Fix issue [#187](https://github.com/didi/booster/issues/187)
- Fix issue [#226](https://github.com/didi/booster/issues/226)
- Add property `val dependencies: Collection<String>` to `TransformContext`

## v3.2.0

- Support AGP `4.2`
- Fix AGP version comparison
- Support disabling variant transformation by setting property `booster.transform.${variantName}.enabled` to `false`
- Support generating transform diff by setting property `booster.transform.diff` to `true`
- Refactor `ClassTransformer` API
  - Add property `name`
  - Add method `getReport(TransformContext, String): File`
  - Add method `getReportDir(TransformContext): File`

## v3.1.0

- Support property `booster.task.compression.pngquant.ignores` to ignore resources by wildcard
- Support property `booster.task.compression.cwebp.ignores` to ignore resources by wildcard
- Sanitize exception stack trace caught by `ActivityThreadCallback`

## v3.0.0

- Rebuild AGP compatibility
- Support the latest AGP version `4.1.0`
- AGP integration tests improvement

## v2.4.0

- Fix [#192](https://github.com/didi/booster/issues/192)
- Android Gradle plugin API compatibility improvement

## v2.3.3

- fix issue [#194](https://github.com/didi/booster/issues/194)

## v2.3.2

- fix issue [#194](https://github.com/didi/booster/issues/194)
- JetPack support for shared preferences optimization

## v2.3.1

- fix empty thread name

## v2.3.0

- Add transform outputs verification
- Improves [booster-transform-thread](https://github.com/didi/booster/blob/master/booster-transform-thread)
- Improves threads management for [booster-transform-shared-preferences](https://github.com/didi/booster/blob/master/booster-transform-shared-preferences)
- Fix API compatibility issue of [booster-transform-webview](https://github.com/didi/booster/blob/master/booster-transform-webview)

## v2.2.0

- Improves [booster-transform-thread](https://github.com/didi/booster/blob/master/booster-transform-thread)
- Improves [booster-transform-shared-preferences](https://github.com/didi/booster/blob/master/booster-transform-shared-preferences)

## v2.1.0

- fix bug of [booster-transform-activity-thread](https://github.com/didi/booster/tree/master/booster-transform-activity-thread)

## v2.0.0

- Fix compatibility issues on AGP 3.5
- Improves Android gradle plugin compatibility (AGP 4.0 is supported)
- Refactor `Transformer` & `VariantProcessor` service provider loading

## v1.7.2

- Refactor `Transformer` dynamic discovery and loading

## v1.7.1

- fix: image compression with build cache

## v1.7.0

- Fix `booster-cha` dependencie
- Refactor [booster-transfrom-util](https://github.com/didi/booster/tree/master/booster-transform-util) to improve unit test
- Refactor SPI loading
- Add `dokka` doc comments to generate API reference
- AGP API compatibility improvement


## v1.5.2



## v1.6.0

- fix [#154](https://github.com/didi/booster/issues/154)
- fix [#157](https://github.com/didi/booster/issues/157)
- Catch transform exception caused by broken cla
- Extract CHA from [booster-task-analyser](https://github.com/didi/booster/tree/master/booster-task-analyser) as a standalone module

## v1.5.1

- Fix: [#151](https://github.com/didi/booster/issues/151) Build failed caused by empty transformer
- Fix dex merging error when transform scope is empty

## v1.5.0

1. 修复 *AGP 3.6.0* 兼容性问题 [[#145](https://github.com/didi/booster/issues/145)](https://github.com/didi/booster/issues/145)
1. 修复 *Transform* 增量编译的问题
1. 优化 [booster-transform-activity-thread](https://github.com/didi/booster/blob/master/booster-transform-activity-thread)，支持自定义堆栈包名白名单过滤
1. 增加 [booster-task-analyser](https://github.com/didi/booster/blob/master/booster-task-analyser) 用于替代原来的 *booster-transform-lint* ，不仅在特性上更加丰富，性能表现也是更胜一筹：

    - 全新的类继承分析 *CHA (Class Hierarchy Analysis)* ，分析结果更精准
    - 新增 *AnalyserTask* ，移除 *LintTransformer* ，通过运行单独的 *Task* 进行静态分析，详见：[README.md](https://github.com/didi/booster/blob/master/booster-task-analyser/README.md)
    - 支持 XML layout 分析，检测 layout 中不存在的 class，避免线上崩溃
    - 支持 `@UiThread` 和 `@MainThread`
    - 支持 *EventBus* `@Subscribe`
    - 支持自定义黑名单和白名单

1. 新增 [booster-api](https://github.com/didi/booster/blob/master/booster-api) 模块，便于 feature 开发和单元测试

## v1.4.0

- Improves [booster-task-compression-cwebp](https://github.com/didi/booster/blob/master/booster-task-compression-cwebp) to support build cache
- Improves [booster-task-compression-pngquant](https://github.com/didi/booster/blob/master/booster-task-compression-pngquant) to support build cache
- Improves transform to support build cache
- Improves [booster-command](https://github.com/didi/booster/blob/master/booster-command) installation to support build cache

## v1.3.2

- Fix issue [#138](https://github.com/didi/booster/issues/138)
- Improve BR inlining
- Upgrade auto-service version to *1.0-rc6*
- Upgrade gradle version to *6.2*
- Remove `uploadArchives` task

## v1.3.1

1. Rethrow exception with file path when [booster-task-compression-cwebp](https://github.com/didi/booster/blob/master/booster-task-compression-cwebp) detect image transparency failed
1. Handle rejected execution from parallel scatter zip creator

## v1.3.0

1. Add [booster-command](https://github.com/didi/booster/blob/master/booster-command) to support run external prebuilt command
1. Refactor resource compression to support [booster-task-compression-cwebp](https://github.com/didi/booster/blob/master/booster-task-compression-cwebp) & [booster-task-compression-pngquant](https://github.com/didi/booster/blob/master/booster-task-compression-pngquant) work togethor
1. Improve transform performance

## v1.2.1

- Prevent `res/raw` resources from compression
- Refactor transformers to make them more unit test friendly

## v1.2.0

- Fix R class files removal issue [#124](https://github.com/didi/booster/issues/124)
- Add [booster-transform-br-inline](https://github.com/didi/booster/blob/master/booster-transform-br-inline) to support BR inline for data binding
- Add [booster-transform-verifier](https://github.com/didi/booster/blob/master/booster-transform-verifier) to support verify bytecode
- Add `@Priority` to support ordering transformer
- Refactor transform process to improve build performance
- Support CPU time statistics for transformer
- Remove `booster-task-all` and `booster-transform-all`

## v1.1.1

- Fix bug of [transform-task-compression-pngquant](https://github.com/didi/booster/blob/master/booster-task-compression-pngquant)

## v1.1.0

- Refactor & optimize resource compression
- Refactor transform SPI

## v1.0.0

- Improves transform performance 50%+
- Improves [booster-task-compression](https://github.com/didi/booster/blob/master/booster-task-compression) performance
- [booster-transform-thread](https://github.com/didi/booster/blob/master/booster-transform-thread) supports enable/disable thread pool optimization
- Fix bugs of [booster-transform-shared-preferences](https://github.com/didi/booster/blob/master/booster-transform-shared-preferences)

## v0.28.0

- Fix issue [#119](https://github.com/didi/booster/issues/119)

## v0.27.0

- Supporting dynamic feature module [#109](https://github.com/didi/booster/issues/109)
- SharedPreferences optimization improvement

## v0.26.1

- Fix compatibility of `booster-transform-r-inline`
- Fix [#103](https://github.com/didi/booster/issues/103)

## v0.26.0

- Fix issue [#102](https://github.com/didi/booster/issues/102), [#103](https://github.com/didi/booster/issues/103)

## v0.25.0

- Fix issue
  - [#77](https://github.com/didi/booster/issues/77)
  - [#100](https://github.com/didi/booster/issues/100)

## v0.24.0

- Fix issue [#99](https://github.com/didi/booster/issues/99)

## v0.23.0

- Deprecates constant field removal

## v0.22.0

- Fix duplicated entry caused by incremental build

## v0.21.3

- Fix issue: Process "command `cacls`" finished with non-zero exit value 87. [#29](https://github.com/didi/booster/issues/29), [#70](https://github.com/didi/booster/issues/70), [#80](https://github.com/didi/booster/issues/80)

## v0.21.2

- Fix issue [#80](https://github.com/didi/booster/issues/80)

## v0.21.1

- Fix issue [#78](https://github.com/didi/booster/issues/78)

## v0.21.0

- Fix compatibility of AAPT2 especially for Android gradle plugin 3.1.x

## v0.20.0

- Add [booster-task-list-shared-library](./booster-task-list-shared-library)

## v0.19.0

- Fix bug of [ActivityThreadTransformer](./booster-transform-activity-thread/src/main/kotlin/com/didiglobal/booster/transform/activitythread/ActivityThreadTransformer.kt)

## v0.18.0

- Fix bug of [ActivityThreadCallback](./booster-android-instrument-activity-thread/src/main/java/com/didiglobal/booster/instrument/ActivityThreadCallback.java)

## v0.17.0

- Add module [booster-transform-javassist](./booster-transform-javassist) to support bytecode manipulation using Javassist

## v0.16.0

- Fix issue [#66](https://github.com/didi/booster/issues/66)

## v0.15.0

- Fix issue [#67](https://github.com/didi/booster/issues/67)

## v0.14.0

- Fix issue: shrinking without replace retained symbol

## v0.13.0

- Add module [booster-transform-activity-thread](booster-transform-activity-thread) for *ActivityThread* hooking
- Add module [booster-android-instrument-activity-thread](booster-android-instrument-activity-thread) for *ActivityThread* instrumenting

## v0.12.0

- Add module [booster-transform-logcat](booster-transform-logcat) for logcat intercepting
- Add module [booster-android-instrument-logcat](booster-android-instrument-logcat) for log API instrumenting

## v0.11.0

- Fix compatibility issues with Android gradle plugin 3.1.x and 3.4+: [#45](https://github.com/didi/booster/issues/45)

## v0.10.0

- Add `booster-transform-finalizer-watchdog-daemon` to avoid crash by `TimeoutException`

## v0.9.0

- Add *booster-transform-res-check* for resource & assets checking

## v0.8.0

- Optimization for `AsyncTask`
- Improvement for *booster-transform-shrink*

## v0.7.0

- Add Webview preloading

## v0.6.0

- Fix bug of dex builder in  incremental build

## v0.5.0

- Fix issue [#33](https://github.com/didi/booster/issues/33), [#34](https://github.com/didi/booster/issues/34)
- Optimization for `MediaPlayer`

## v0.4.5

- Fix issue [#32](https://github.com/didi/booster/issues/32)

## v0.4.4

- Fix issue [#29](https://github.com/didi/booster/issues/29)
- Ignores WebP compression of launcher icon

## v0.4.3

- Fix [#28](https://github.com/didi/booster/issues/28)

## v0.4.2

- Fix issue [#27](https://github.com/didi/booster/issues/27)

## v0.4.1

- Fix bugs [#23](https://github.com/didi/booster/issues/23)
- Update Lint graph layout [#22](https://github.com/didi/booster/issues/22)

## v0.4.0

- Supports WebP compression

  Since v0.4.0, Booster provides built-in libwebp binaries for resources compression, so, you don't have to install the *cwebp* executable manually,  about the usage, please see [booster-task-compression](../master/booster-task-compression).

## v0.3.3

- Fix compression report
- Add more INFO logs for class transforming

## v0.3.2

- Fix issue [#17](https://github.com/didi/booster/issues/17), [#19](https://github.com/didi/booster/issues/19)
- Resources & assets compression improvement
- Redundant resources reduction

## v0.3.1

- Fix issue [#18](https://github.com/didi/booster/issues/18)
- Supports compressing by `pngquant`
- Improves assets and resources compression

## v0.3.0

- Add `booster-task-compression` for resources compression

## v0.2.1

- Fix issue [#3](https://github.com/didi/booster/issues/3)
- Fix issue [#5](https://github.com/didi/booster/issues/5)

## v0.2.0

- Fix issue [#5](https://github.com/didi/booster/issues/5)
- Additional options for `booster-transform-shrink` [#8](https://github.com/didi/booster/issues/8)
- Additional options for `booster-transform-lint`
- Thread renaming
- Multithreading optimization
- Shared preferences optimization
- Build report

## v0.1.6



## v0.1.5

- Fix issue [#10](https://github.com/didi/booster/issues/10)
- Fix issue [#13](https://github.com/didi/booster/issues/13)
- Optimization for shared preferences editor

## v0.1.4

- Fix kotlin API compatibility [#6](https://github.com/didi/booster/issues/6), [#9](https://github.com/didi/booster/issues/9)
- Retaining resource ids references by layout

## v0.1.3

- Fix issue [#4](https://github.com/didi/booster/issues/4): WARNING: API 'variant.getJavaCompiler()' is obsolete and has been replaced with 'variant.getJavaCompileProvider()'

## v0.1.2

- Fix issue [#3](https://github.com/didi/booster/issues/3)

## v0.1.1

- Fix generated `REVISION`
- Fix compatibility issues with Android gradle 3.3+ [#2](https://github.com/didi/booster/issues/2)

## v0.1.0

- Performance linting
- Constants shrinking
- Crash fixing (Caused by Toast on Android 7.1.1)
- Miscellaneou
  - Tasks for artifacts displaying
  - Tasks for Android permissions displaying
  - Tasks for dependencies displaying
  - Transformer for API usage displaying

