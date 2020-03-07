package com.didiglobal.booster.task.profile

import com.didiglobal.booster.task.profile.graph.CallGraph

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
 */
internal val EXCLUDES = Regex("^(((android[x]?)|(com/(((google/)?android)|(google/gson)|(didiglobal/booster/instrument))))/.+)|(.+/R(\\$[a-z]+)?)$")

/**
 * Classes annotated with @MainThread
 */
internal val PLATFORM_CLASSES_RUN_ON_MAIN_THREAD = arrayOf(
        "android/app/Application",
        "android/app/Activity",
        "android/app/Service",
        "android/content/BroadcastReceiver",
        "android/content/ContentProvider"
)

/**
 * Classes annotated with @UiThread
 */
internal val PLATFORM_CLASSES_RUN_ON_UI_THREAD = arrayOf(
        "android/view/View",
        "android/widget/Magnifier"
)

internal val PLATFORM_METHODS_RUN_ON_UI_THREAD = arrayOf<CallGraph.Node>(

).toSet()

internal val PLATFORM_METHODS_RUN_ON_MAIN_THREAD = arrayOf(
        "android/app/Activity.onCreate(Landroid/os/Bundle;)V",
        "android/os/AsyncTask.onPreExecute()V",
        "android/os/AsyncTask.onPostExecute(Ljava/lang/Object;)V",
        "android/os/AsyncTask.onProgressUpdate([Ljava/lang/Object;)V",
        "android/os/AsyncTask.onCancelled([Ljava/lang/Object;)V",
        "android/os/AsyncTask.onCancelled()V",
        "android/os/AsyncTask.execute([Ljava/lang/Object;)Landroid/os/AsyncTask;"
).map(CallGraph.Node.Companion::valueOf).toSet()

internal val MAIN_THREAD_ANNOTATIONS = arrayOf(
        "Landroidx/annotation/MainThread;",
        "Landroid/support/annotation/MainThread;",
        "Landroid/annotation/MainThread;"
)

internal val UI_THREAD_ANNOTATIONS = arrayOf(
        "Landroidx/annotation/UiThread;",
        "Landroid/support/annotation/UiThread;",
        "Landroid/annotation/UiThread;"
)

internal val WORKER_THREAD_ANNOTATIONS = arrayOf(
        "Landroidx/annotation/WorkerThread;",
        "Landroid/support/annotation/WorkerThread;",
        "Landroid/annotation/WorkerThread;"
)

internal val BINDER_THREAD_ANNOTATIONS = arrayOf(
        "Landroidx/annotation/BinderThread;",
        "Landroid/support/annotation/BinderThread;",
        "Landroid/annotation/BinderThread;"
)

internal val ANY_THREAD_ANNOTATIONS = arrayOf(
        "Landroidx/annotation/AnyThread;",
        "Landroid/support/annotation/AnyThread;",
        "Landroid/annotation/AnyThread;"
)
