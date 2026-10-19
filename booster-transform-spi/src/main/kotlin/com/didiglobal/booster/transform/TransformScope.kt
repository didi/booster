package com.didiglobal.booster.transform

/**
 * Represents the scope of inputs a transformer requires
 *
 * @author johnsonlee
 */
enum class TransformScope {

    /**
     * Each class is transformed independently, the transformer only requires the class being transformed,
     * the class hierarchy ([TransformContext.klassPool]), the merged manifests and the symbol list.
     * Such transformers can be applied class by class with AGP instrumentation, which keeps incremental builds fast.
     */
    CLASS,

    /**
     * The transformer requires the whole program: the full classpath, [Collector]s, or all artifacts,
     * so all classes have to be transformed together.
     */
    PROGRAM

}
