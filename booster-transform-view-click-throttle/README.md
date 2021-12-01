# booster-transform-view-click-throttle

This module provides a transformer that used to throttle view click events,it check `View.OnClickListener.onClick` and click lambdas method.


## Properties

| Key                                             | Description                                     | Example |
|-------------------------------------------------|-------------------------------------------------|---------|
| `booster.transform.view.click.throttle.duration` | throttle duration (default: 200 (ms)) | 800        |
| `booster.transform.view.click.throttle.global` | all views share same throttle duration (default: false) |   false      |
| `booster.transform.view.click.throttle.ignores` | comma separated wildcard patterns to ignore	 | com/didi/*,android/*        |
| `booster.transform.view.click.throttle.includes` | comma separated wildcard patterns to include	 | com/didi/*,android/*        |

```bash
./gradlew assembleDebug -Pbooster.transform.thread.optimization.enabled=false -Pbooster.transform.thread.optimization.global=false -Pbooster.transform.thread.optimization.ignores='android/*' -Pbooster.transform.thread.optimization.includes='android/*'
```
or configure in the `gradle.properties`:
```properties
booster.transform.view.click.throttle.duration=1000
booster.transform.view.click.throttle.global=false
booster.transform.view.click.throttle.ignores=com/didi/*
// booster.transform.view.click.throttle.includes=com/my/*
```

## Custom duration

Add throttleUtil:
```gradle
implementation "com.didiglobal.booster:booster-android-instrument-view-click-throttle$booster_version"
```

Add `@Throttle(duration = xx)` Annotation to OnClickListener.onClick method, this onClick will work on this duration.
if duration <= 0, will not execute throttle.
```kotlin
viewB.setOnClickListener(object : View.OnClickListener {
    @Throttle(duration = 500L)
    override fun onClick(v: View?) {
        //
    }
})
```

