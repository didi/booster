# booster-task-graph

Generate task graph in [dot](https://graphviz.org/), please make sure you have installed the [dot command line](https://graphviz.org/doc/info/command.html).

## Getting Started

Executing tasks and then find the `*.dot` and `*.dot.png` files under `${rootProject}/build` directory, for example, if I run the following command line

```bash
./gradlew assembleDebug --dry-run
```

Then, the following files will be generated under `${rootProject}/build`:

* `assembleDebug.dot`
* `assembleDebug.dot.png`
