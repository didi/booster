# booster-task-compression

This module is used for assets and resource compression

本模块用于对资源进行压缩

## Properties

The following table shows the properties that transformer supports:

| Property                            | Description                                                  | Example                            |
| ----------------------------------- | ------------------------------------------------------------ | ---------------------------------- |
| `booster.task.compression.pngquant` | colon separated paths (env.PATH in default)                  | /opt/pngquant/bin                  |

The properties can be passthrough the command line as following:

```bash
./gradlew assembleDebug -Pbooster.task.compression.pngquant=/opt/pngquant/bin
```

or configured in the `gradle.properties`:

```properties
booster.task.compression.pngquant=/opt/pngquant/bin
```
