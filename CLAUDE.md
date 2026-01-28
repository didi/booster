# Booster 项目指南

## 项目概述

Booster 是滴滴开源的 Android 应用质量优化框架，通过 Gradle 插件 + SPI 机制实现可扩展的字节码转换和任务处理。

**核心功能**：性能检测、系统 Bug 修复、多线程优化、应用瘦身（资源去重、R 类内联等）

**效果**：稳定性提升 15%~25%，包体积减小 1MB~10MB

## 技术栈

- **语言**: Kotlin (主) + Java
- **构建**: Gradle 8.5 + Kotlin DSL
- **字节码操作**: ASM + Javassist 3.30.2-GA
- **兼容性**: AGP 8.0 ~ 9.0 (v8_0, v8_1, v8_2, v8_3, v8_4, v8_5, v8_6, v8_7, v8_8, v8_9, v8_10, v8_12, v9_0)
- **发布**: Maven Central (com.didiglobal.booster)

## 模块架构 (48 个模块)

### 核心框架
- `booster-gradle-plugin` - Gradle 插件入口
- `booster-api` - 核心 API 和工具类
- `booster-annotations` - 注解定义

### SPI 层 (扩展接口)
- `booster-transform-spi` - 字节码转换接口 (`Transformer`, `TransformContext`, `KlassPool`)
- `booster-task-spi` - 任务处理器接口 (`VariantProcessor`)

### 字节码转换工具
- `booster-transform-asm` - ASM 实现
- `booster-transform-javassist` - Javassist 实现
- `booster-transform-util` - 工具库

### AGP 兼容层
- `booster-android-gradle-api` - API 抽象
- `booster-android-gradle-compat` - 兼容支持
- `booster-android-gradle-v8_*` - AGP 版本适配 (v8_0 ~ v8_12)
- `booster-android-gradle-v9_0` - AGP 9.0 适配

### 转换实现 (主要功能模块)
- `booster-transform-thread` - 多线程优化
- `booster-transform-toast` - Toast 崩溃修复
- `booster-transform-r-inline` - R 类常量内联
- `booster-transform-res-check` - 资源检查
- `booster-transform-activity-thread` - ActivityThread 优化
- `booster-transform-finalizer-watchdog-daemon` - Finalizer 优化
- `booster-transform-logcat` - Logcat 优化
- `booster-transform-media-player` - MediaPlayer 优化

### 运行时仪器 (配合转换模块使用)
- `booster-android-instrument-*` - 各功能的运行时 Hook 实现

### 辅助工具
- `booster-cha` / `booster-cha-asm` - Class Hierarchy Analysis
- `booster-graph-*` - 任务图可视化
- `booster-task-list-*` - 任务列表工具
- `booster-aapt2` - AAPT2 集成

## 关键入口文件

| 文件 | 路径 | 作用 |
|------|------|------|
| `BoosterPlugin.kt` | `booster-gradle-plugin/.../gradle/` | 插件主类，实现 `Plugin<Project>` |
| `BoosterTransformTask.kt` | 同上 | 字节码转换任务 |
| `Transformer.kt` | `booster-transform-spi/.../transform/` | 转换器接口定义 |
| `TransformContext.kt` | 同上 | 转换上下文 |
| `VariantProcessor.kt` | `booster-task-spi/.../task/spi/` | 变种处理器接口 |
| `AsmTransformer.kt` | `booster-transform-asm/.../asm/` | ASM 转换器实现 |
| `ClassTransformer.kt` | 同上 | 类转换抽象类 |

## 常用命令

```bash
# 构建项目
./gradlew build

# 发布到本地 Maven
./gradlew publishToMavenLocal

# 运行测试
./gradlew test

# 运行集成测试
./gradlew integrationTest

# 编译集成测试（验证兼容性）
./gradlew compileIntegrationTestKotlin

# 查看所有任务
./gradlew tasks
```

## AGP Version Compatibility Development

### Kotlin 2.0 Metadata Compatibility

AGP 8.9+ is compiled with Kotlin 2.0, producing Kotlin metadata incompatible with Kotlin 1.x. Solution:

1. **Gradle Version**: Use Gradle 8.5+ (includes Kotlin 1.9.20 which better handles Kotlin 2.0 metadata)
2. **Compiler Args**: Add to v8_9+ module's `build.gradle`:
   ```groovy
   tasks.withType(org.jetbrains.kotlin.gradle.tasks.KotlinCompile).configureEach {
       kotlinOptions {
           freeCompilerArgs += ['-Xskip-metadata-version-check']
       }
   }
   ```

### Adding New AGP Version Support

1. Create module `booster-android-gradle-v{major}_{minor}`
2. Copy existing module structure (e.g., v8_2)
3. Update AGP dependency version and SDK tools version
4. Implement `AGPInterface`, handling API changes
5. Create `src/integrationTest/` directory with:
   - `kotlin/.../V{major}{minor}IntegrationTest.kt` - Test class
   - `resources/` - Test resources (app.gradle, lib.gradle, buildSrc/, src/)
6. Create `src/e2eTest/` directory with standalone Android test project

### Integration Test Structure

Each AGP adapter module contains:
- `src/integrationTest/` - Gradle TestKit integration tests
- `src/e2eTest/` - Standalone Android project for end-to-end testing

### Running Integration Tests

Integration tests require booster artifacts in local Maven:
```bash
# First, publish to local Maven
./gradlew publishToMavenLocal

# Then run integration tests
./gradlew integrationTest
```

## 开发扩展

### 实现自定义 Transformer

1. 创建模块，依赖 `booster-transform-spi` 和 `booster-transform-asm`
2. 继承 `ClassTransformer` 实现转换逻辑
3. 在 `META-INF/services/` 注册 SPI

### 实现自定义 VariantProcessor

1. 创建模块，依赖 `booster-task-spi`
2. 实现 `VariantProcessor` 接口
3. 在 `META-INF/services/` 注册 SPI

## 代码规范

- 包名前缀: `com.didiglobal.booster`
- 使用 Kotlin 编写新代码
- 遵循 Kotlin 官方编码规范
