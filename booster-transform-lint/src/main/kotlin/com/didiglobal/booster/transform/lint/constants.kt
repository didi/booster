package com.didiglobal.booster.transform.lint

internal val SENSITIVES = arrayOf(
        "activity",
        "fragment",
        "dialog",
        "view",
        "widget",
        "layout"
)

internal val IGNORES = arrayOf(
        "android/support/*",
        "androidx/*",
        "android/arch/*",
        "android/car/*",
        "android/databinding/*",
        "com/android/*",
        "com/google/android/*"
)

/**
 * Main/UI thread entry point of Application
 */
internal val APPLICATION_ENTRY_POINTS = arrayOf(
        "onConfigurationChanged(Landroid/content/res/Configuration;)V",
        "onCreate()V",
        "onLowMemory()V",
        "onTerminate()V",
        "onTrimMemory(I)V"
).map(EntryPoint.Companion::valueOf).toSet()

/**
 * Main/UI thread entry point of Activity
 */
internal val ACTIVITY_ENTRY_POINTS = arrayOf(
        // <editor-fold desc="public methods of Activity">
        "onActionModeFinished(Landroid/view/ActionMode;)V",
        "onActionModeStarted(Landroid/view/ActionMode;)V",
        "onActivityReenter(ILandroid/content/Intent;)V",
        "onAttachFragment(Landroid/app/Fragment;)V",
        "onAttachedToWindow()V",
        "onBackPressed()V",
        "onConfigurationChanged(Landroid/content/res/Configuration;)V",
        "onContentChanged()V",
        "onContextItemSelected(Landroid/view/MenuItem;)Z",
        "onContextMenuClosed(Landroid/view/Menu;)V",
        "onCreate(Landroid/os/Bundle;Landroid/os/PersistableBundle;)V",
        "onCreateContextMenu(Landroid/view/ContextMenu;Landroid/view/View;Landroid/view/ContextMenu\$ContextMenuInfo;)V",
        "onCreateDescription()Ljava/lang/CharSequence;",
        "onCreateNavigateUpTaskStack(Landroid/app/TaskStackBuilder;)V",
        "onCreateOptionsMenu(Landroid/view/Menu;)Z",
        "onCreatePanelMenu(ILandroid/view/Menu;)Z",
        "onCreatePanelView(I)Landroid/view/View;",
        "onCreateThumbnail(Landroid/graphics/Bitmap;Landroid/graphics/Canvas;)Z",
        "onCreateView(Landroid/view/View;Ljava/lang/String;Landroid/content/Context;Landroid/util/AttributeSet;)Landroid/view/View;",
        "onCreateView(Ljava/lang/String;Landroid/content/Context;Landroid/util/AttributeSet;)Landroid/view/View;",
        "onDetachedFromWindow()V",
        "onEnterAnimationComplete()V",
        "onGenericMotionEvent(Landroid/view/MotionEvent;)Z",
        "onKeyDown(ILandroid/view/KeyEvent;)Z",
        "onKeyLongPress(ILandroid/view/KeyEvent;)Z",
        "onKeyMultiple(IILandroid/view/KeyEvent;)Z",
        "onKeyShortcut(ILandroid/view/KeyEvent;)Z",
        "onKeyUp(ILandroid/view/KeyEvent;)Z",
        "onLocalVoiceInteractionStarted()V",
        "onLocalVoiceInteractionStopped()V",
        "onLowMemory()V",
        "onMenuItemSelected(ILandroid/view/MenuItem;)Z",
        "onMenuOpened(ILandroid/view/Menu;)Z",
        "onMultiWindowModeChanged(Z)V",
        "onMultiWindowModeChanged(ZLandroid/content/res/Configuration;)V",
        "onNavigateUp()Z",
        "onNavigateUpFromChild(Landroid/app/Activity;)Z",
        "onOptionsItemSelected(Landroid/view/MenuItem;)Z",
        "onOptionsMenuClosed(Landroid/view/Menu;)V",
        "onPanelClosed(ILandroid/view/Menu;)V",
        "onPictureInPictureModeChanged(ZLandroid/content/res/Configuration;)V",
        "onPictureInPictureModeChanged(Z)V",
        "onPostCreate(Landroid/os/Bundle;Landroid/os/PersistableBundle;)V",
        "onPrepareNavigateUpTaskStack(Landroid/app/TaskStackBuilder;)V",
        "onPrepareOptionsMenu(Landroid/view/Menu;)Z",
        "onPreparePanel(ILandroid/view/View;Landroid/view/Menu;)Z",
        "onProvideAssistContent(Landroid/app/assist/AssistContent;)V",
        "onProvideAssistData(Landroid/os/Bundle;)V",
        "onProvideKeyboardShortcuts(Ljava/util/List;Landroid/view/Menu;I)V",
        "onProvideReferrer()Landroid/net/Uri;",
        "onRequestPermissionsResult(ILjava/lang/String;[I)V",
        "onRestoreInstanceState(Landroid/os/Bundle;Landroid/os/PersistableBundle;)V",
        "onRetainNonConfigurationInstance()Ljava/lang/Object;",
        "onSaveInstanceState(Landroid/os/Bundle;Landroid/os/PersistableBundle;)V",
        "onSearchRequested(Landroid/view/SearchEvent;)Z",
        "onSearchRequested()Z",
        "onStateNotSaved()V",
        "onTopResumedActivityChanged(Z)V",
        "onTouchEvent(Landroid/view/MotionEvent;)Z",
        "onTrackballEvent(Landroid/view/MotionEvent;)Z",
        "onTrimMemory(I)V",
        "onUserInteraction()V",
        "onVisibleBehindCanceled()V",
        "onWindowAttributesChanged(Landroid/view/WindowManager\$LayoutParams;)V",
        "onWindowFocusChanged(Z)V",
        "onWindowStartingActionMode(Landroid/view/ActionMode\$Callback;I)Landroid/view/ActionMode;",
        "onWindowStartingActionMode(Landroid/view/ActionMode\$Callback;)Landroid/view/ActionMode;",
        // </editor-fold>
        // <editor-fold desc="protected methods of Activity">
        "attachBaseContext(Landroid/content/Context;)V",
        "onActivityResult(IILandroid/content/Intent;)V",
        "onApplyThemeResource(Landroid/content/res/Resources\$Theme;IZ)V",
        "onChildTitleChanged(Landroid/app/Activity;Ljava/lang/CharSequence;)V",
        "onCreate(Landroid/os/Bundle;)V",
        "onCreateDialog(I)Landroid/app/Dialog;",
        "onCreateDialog(ILandroid/os/Bundle;)Landroid/app/Dialog;",
        "onDestroy()V",
        "onNewIntent(Landroid/content/Intent;)V",
        "onPause()V",
        "onPostCreate(Landroid/os/Bundle;)V",
        "onPostResume()V",
        "onPrepareDialog(ILandroid/app/Dialog;Landroid/os/Bundle;)V",
        "onPrepareDialog(ILandroid/app/Dialog;)V",
        "onRestart()V",
        "onRestoreInstanceState(Landroid/os/Bundle;)V",
        "onResume()V",
        "onSaveInstanceState(Landroid/os/Bundle;)V",
        "onStart()V",
        "onStop()V",
        "onTitleChanged(Ljava/lang/CharSequence;I)V",
        "onUserLeaveHint()V"
        // </editor-fold>
).map(EntryPoint.Companion::valueOf).toSet()

/**
 * Main/UI thread entry point of Service
 */
internal val SERVICE_ENTRY_POINTS = arrayOf(
        "onConfigurationChanged(Landroid/content/res/Configuration;)V",
        "onCreate()V",
        "onDestroy()V",
        "onLowMemory()V",
        "onRebind(Landroid/content/Intent;)V",
        "onStart(Landroid/content/Intent;I)V",
        "onStartCommand(Landroid/content/Intent;II)I",
        "onTaskRemoved(Landroid/content/Intent;)V",
        "onTrimMemory(I)V",
        "onUnbind(Landroid/content/Intent;)Z"
).map(EntryPoint.Companion::valueOf).toSet()

/**
 * Main/UI thread entry point of Receiver
 */
internal val RECEIVER_ENTRY_POINTS = arrayOf(
        "onReceive(Landroid/content/Context;Landroid/content/Intent;)V"
).map(EntryPoint.Companion::valueOf).toSet()

/**
 * Main/UI thread entry point of Provider
 */
internal val PROVIDER_ENTRY_POINTS = arrayOf(
        "onConfigurationChanged(Landroid/content/res/Configuration;)V",
        "onCreate()Z",
        "onLowMemory()V",
        "onTrimMemory(I)V"
).map(EntryPoint.Companion::valueOf).toSet()

internal val CLASSES_RUN_ON_MAIN_THREAD = setOf(
        "android/view/View",
        "android/view/ViewParent",
        "android/view/ViewManager",
        "android/content/DialogInterface",
        "androidx/fragment/app/Fragment",
        "android/support/v4/app/Fragment"
)

internal val MAIN_THREAD_ANNOTATIONS = arrayOf(
        "Landroidx/annotation/MainThread;",
        "Landroidx/annotation/UiThread;",
        "Landroid/support/annotation/MainThread;",
        "Landroid/support/annotation/UiThread;"
)
