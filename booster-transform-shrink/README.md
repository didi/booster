# booster-transform-shrink

This module is used for constants shrinking, such as fields in `BuildConfig`, `R$id`, `R$layout`, `R$string`, etc.

## Properties

The following table shows the properties that transformer supports:

| Property                         | Description                                                  | Example                            |
| -------------------------------- | ------------------------------------------------------------ | ---------------------------------- |
| `booster.transform.shrink.ignores` | comma separated wildcard patterns to ignore                  | android/\*,androidx/\*             |

The properties can be passthrough the command line as following:

```bash
./gradlew assembleDebug -Pbooster.transform.shrink.ignores=android/*,androidx/*
```

or configured in the `gradle.properties`:

```properties
booster.transform.shrink.ignores=android/*,androidx/*
```

