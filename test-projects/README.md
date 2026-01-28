# AGP Test Projects

This directory contains standalone Android test projects for different AGP (Android Gradle Plugin) versions.

## Projects

| Directory | AGP Version | Gradle Version | Kotlin Version | Status |
|-----------|-------------|----------------|----------------|--------|
| `agp-8_3_0` | 8.3.0 | 8.4 | 1.9.22 | ✅ |
| `agp-8_4_0` | 8.4.0 | 8.6 | 1.9.22 | ✅ |
| `agp-8_5_0` | 8.5.0 | 8.7 | 1.9.22 | ✅ |
| `agp-8_6_0` | 8.6.0 | 8.7 | 1.9.24 | ✅ |
| `agp-8_7_0` | 8.7.0 | 8.9 | 1.9.24 | ✅ |
| `agp-8_8_0` | 8.8.0 | 8.10.2 | 2.0.0 | ✅ |
| `agp-8_9_0` | 8.9.0 | 8.11.1 | 2.0.21 | ✅ |
| `agp-8_10_0` | 8.10.0 | 8.11.1 | 2.0.21 | ✅ |
| `agp-8_12_0` | 8.12.0 | 8.13 | 2.0.21 | ✅ |
| `agp-9_0_0` | 9.0.0 | 9.1.0 | Built-in | ✅ |

## Usage

### Build a specific project

```bash
cd agp-8_3_0
./gradlew assembleDebug
```

### Build all projects

```bash
for dir in agp-*; do
    echo "Building $dir..."
    (cd "$dir" && ./gradlew assembleDebug --no-daemon)
done
```

## Requirements

- JDK 17 or higher
- Android SDK with appropriate build tools
- Set `ANDROID_HOME` environment variable

## Notes

- Each project is independent and can be built separately
- AGP 8.9+ requires Kotlin 2.0+
- AGP 9.0+ has significant breaking changes from previous versions
