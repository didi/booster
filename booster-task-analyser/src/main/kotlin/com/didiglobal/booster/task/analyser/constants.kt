package com.didiglobal.booster.task.analyser

import com.didiglobal.booster.task.analyser.graph.CallGraph

private const val DOLLAR = '$'

/**
 * The following classes exclude from lint
 *
 * - `android.**`
 * - `androidx.**`
 * - `com.android.**`
 * - `com.google.android.**`
 * - `com.google.gson.**`
 * - `com.didiglobal.booster.instrument.**`
 * - `**.R`
 * - `**.R$*`
 * - `BuildConfig`
 */
internal val EXCLUDES = Regex("^(((android[x]?)|(com/(((google/)?android)|(google/gson)|(didiglobal/booster/instrument))))/.+)|(.+/((R[2]?(${DOLLAR}[a-z]+)?)|(BuildConfig)))$")


internal val PLATFORM_METHODS_RUN_ON_UI_THREAD = arrayOf<CallGraph.Node>(

).toSet()

internal val PLATFORM_METHODS_RUN_ON_MAIN_THREAD = arrayOf(
        "android/os/AsyncTask.onPreExecute()V",
        "android/os/AsyncTask.onPostExecute(Ljava/lang/Object;)V",
        "android/os/AsyncTask.onProgressUpdate([Ljava/lang/Object;)V",
        "android/os/AsyncTask.onCancelled([Ljava/lang/Object;)V",
        "android/os/AsyncTask.onCancelled()V",
        "android/os/AsyncTask.execute([Ljava/lang/Object;)Landroid/os/AsyncTask;"
).map(CallGraph.Node.Companion::valueOf).toSet()

internal val MAIN_THREAD_ANNOTATIONS = arrayOf(
        "androidx/annotation/MainThread",
        "android/support/annotation/MainThread",
        "android/annotation/MainThread"
)

internal val UI_THREAD_ANNOTATIONS = arrayOf(
        "androidx/annotation/UiThread",
        "android/support/annotation/UiThread",
        "android/annotation/UiThread"
)

internal val WORKER_THREAD_ANNOTATIONS = arrayOf(
        "androidx/annotation/WorkerThread",
        "android/support/annotation/WorkerThread",
        "android/annotation/WorkerThread"
)

internal val BINDER_THREAD_ANNOTATIONS = arrayOf(
        "androidx/annotation/BinderThread",
        "android/support/annotation/BinderThread",
        "android/annotation/BinderThread"
)

internal val ANY_THREAD_ANNOTATIONS = arrayOf(
        "androidx/annotation/AnyThread",
        "android/support/annotation/AnyThread",
        "android/annotation/AnyThread"
)