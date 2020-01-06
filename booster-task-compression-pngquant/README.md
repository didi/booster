# booster-task-compression-pngquant

This module is used for assets and resource compression using `pngquant`

- If `pngquant` has been installed, then use `pngquant`
- otherwise, do not compress any resources

## Properties

The following table shows the properties that transformer supports:

| Property                                           | Description                                                  | Example                            |
| -------------------------------------------------- | ------------------------------------------------------------ | ---------------------------------- |
| `booster.task.compression.pngquant.bin`            | colon separated paths (env.PATH in default)                  | /opt/pngquant/bin                  |
| `booster.task.compression.pngquant.option.quality` | compression quality (this default is 80)                     |                                    | 
| `booster.task.compression.pngquant.option.speed`   | compression speed (the default is 3)                         |                                    | 

## Using `pngquant`

```bash
./gradlew assembleDebug \
    -Pbooster.task.compression.pngquant.bin=/opt/pngquant/bin \
    -Pbooster.task.compression.pngquant.option.quality=75 \
    -Pbooster.task.compression.pngquant.option.speed=1
```

or configured in the `gradle.properties`:

```properties
booster.task.compression.pngquant.bin=/opt/pngquant/bin
booster.task.compression.pngquant.option.quality=75
booster.task.compression.pngquant.option.speed=1
```
