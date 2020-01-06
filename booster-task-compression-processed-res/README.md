# booster-task-compression

This module is used for assets and resource compression, Booster provides two compression tools, `pngquant` and `cwebp` (built-in), which compressor to use as the default depends on the `minSdkVersion`.

- If specified property `booster.task.compression.compressor`, then use the specified one
- If  `minSdkVersion` &gt;= 18, then use `cwebp`
- If 15 &lt;= `minSdkVersion` &lt;= 17, then use `cwebp` but only compress opaque images, any image has transparency will be ignored
- If `pngquant` has been installed, then use `pngquant`
- otherwise, do not compress any resources

本模块用于对资源进行压缩，Booster 提供了两种压缩工具：`pngquant` 和 `cwebp`（内置），选择哪个压缩器作为默认的，这取决于 `minSdkVersion`

## Properties

The following table shows the properties that transformer supports:

| Property                              | Description                                                  | Example                            |
| ------------------------------------- | ------------------------------------------------------------ | ---------------------------------- |
| `booster.task.compression.compressor` | the compressor to apply (supports `pngquant` and `cwebp`)    | pngquant                           |
| `booster.task.compression.pngquant`   | colon separated paths (env.PATH in default)                  | /opt/pngquant/bin                  |

### Using `pngquant`

```bash
./gradlew assembleDebug -Pbooster.task.compression.compressor=pngquant -Pbooster.task.compression.pngquant=/opt/pngquant/bin
```

or configured in the `gradle.properties`:

```properties
booster.task.compression.compressor=pngquant
booster.task.compression.pngquant=/opt/pngquant/bin
```

### Using `cwebp`

```bash
./gradlew assembleDebug -Pbooster.task.compression.compressor=cwebp
```

or configured in the `gradle.properties`:

```properties
booster.task.compression.compressor=cwebp
```
