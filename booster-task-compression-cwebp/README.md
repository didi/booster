# booster-task-compression-cwebp

This module is used for assets and resource compression using `cwebp`

## Properties

The following table shows the properties that transformer supports:

| Property                                 | Description                              |
| ---------------------------------------- | ---------------------------------------- |
| `booster.task.compression.cwebp.quality` | compression quality (the default is 80)  |

## Using `cwebp`

```bash
./gradlew assembleDebug -Pbooster.task.compression.cwebp.quality=75
```

or configured in the `gradle.properties`:

```properties
booster.task.compression.cwebp.quality=75
```
