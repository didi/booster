# booster-transform-thread

This module is used for multi-threading optimization.

## Properties

| Key                                             | Description                                     | Example |
|-------------------------------------------------|-------------------------------------------------|---------|
| `booster.transform.thread.optimization.enabled` | Enable thread pool optimization (default: true) |         |

## Disable thread pool optimization

```bash
./gradlew assembleDebug -Pbooster.transform.thread.optimization.enabled=false
```

or configure in the `gradle.properties`:
   
```properties
booster.transform.thread.optimization.enabled=false
```



