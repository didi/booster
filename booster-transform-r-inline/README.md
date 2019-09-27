# booster-transform-r-inline

This module is used for resource index inline, such as fields in `R$id`, `R$layout`, `R$string`, etc.

## Properties

The following table shows the properties that transformer supports:

| Property                         | Description                                                  | Example                            |
| -------------------------------- | ------------------------------------------------------------ | ---------------------------------- |
| `booster.transform.r.inline.ignores` | comma separated wildcard patterns to ignore                  | android/\*,androidx/\*             |

The properties can be passthrough the command line as following:

```bash
./gradlew assembleDebug -Pbooster.transform.r.inline.ignores=android/*,androidx/*
```

or configured in the `gradle.properties`:

```properties
booster.transform.r.inline.ignores=android/*,androidx/*
```

