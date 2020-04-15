package com.didiglobal.booster.task.analyser

import com.didiglobal.booster.cha.graph.CallGraph

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


internal val PLATFORM_METHODS_RUN_ON_UI_THREAD = arrayOf(
    "androidx/asynclayoutinflater/view/AsyncLayoutInflater.inflate(ILandroid/view/ViewGroup;Landroidx/asynclayoutinflater/view/AsyncLayoutInflater${DOLLAR}OnInflateFinishedListener;)V"
).map(CallGraph.Node.Companion::valueOf).toSet()

internal val PLATFORM_METHODS_RUN_ON_MAIN_THREAD = arrayOf(
        "android/content/ContextWrapper.attachBaseContext(Landroid/content/Context;)V",

        "android/os/AsyncTask.onPreExecute()V",
        "android/os/AsyncTask.onPostExecute(Ljava/lang/Object;)V",
        "android/os/AsyncTask.onProgressUpdate([Ljava/lang/Object;)V",
        "android/os/AsyncTask.onCancelled([Ljava/lang/Object;)V",
        "android/os/AsyncTask.onCancelled()V",
        "android/os/AsyncTask.execute([Ljava/lang/Object;)Landroid/os/AsyncTask;",

        "android/view/inputmethod/InputMethod.attachToken(Landroid/os/IBinder;)V",
        "android/view/inputmethod/InputMethod.bindInput(Landroid/view/inputmethod/InputBinding;)V",
        "android/view/inputmethod/InputMethod.unbindInput()V",
        "android/view/inputmethod/InputMethod.startInput(Landroid/view/inputmethod/InputConnection;Landroid/view/inputmethod/EditorInfo;)V",
        "android/view/inputmethod/InputMethod.restartInput(Landroid/view/inputmethod/InputConnection;Landroid/view/inputmethod/EditorInfo;)V",
        "android/view/inputmethod/InputMethod.dispatchStartInputWithToken(Landroid/view/inputmethod/InputConnection;Landroid/view/inputmethod/EditorInfo;ZLandroid/os/IBinder;)V",
        "android/view/inputmethod/InputMethod.createSession(Landroid/view/inputmethod/SessionCallback;)V",
        "android/view/inputmethod/InputMethod.setSessionEnabled(Landroid/view/inputmethod/InputMethodSession;Z)V",
        "android/view/inputmethod/InputMethod.revokeSession(Landroid/view/inputmethod/InputMethodSession;)V",
        "android/view/inputmethod/InputMethod.showSoftInput(ILandroid/os/ResultReceiver;)V",
        "android/view/inputmethod/InputMethod.hideSoftInput(ILandroid/os/ResultReceiver;)V",
        "android/view/inputmethod/InputMethod.changeInputMethodSubtype(Landroid/view/inputmethod/InputMethodSubtype;)V",

        "androidx/fragment/app/Fragment.getViewLifecycleOwner()Landroidx/lifecycle/LifecycleOwner;",

        "androidx/lifecycle/Lifecycle.addObserver(Landroidx/lifecycle/Lifecycle/LifecycleObserver;)V",
        "androidx/lifecycle/Lifecycle.removeObserver(Landroidx/lifecycle/Lifecycle/LifecycleObserver;)V",
        "androidx/lifecycle/Lifecycle.getCurrentState()Landroidx/lifecycle/Lifecycle${DOLLAR}State;",
        "androidx/lifecycle/LifecycleRegistry.markState(Landroidx/lifecycle/Lifecycle${DOLLAR}State;)V"
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