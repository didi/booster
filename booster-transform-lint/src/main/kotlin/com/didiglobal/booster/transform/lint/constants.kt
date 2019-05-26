package com.didiglobal.booster.transform.lint

import com.didiglobal.booster.transform.lint.graph.CallGraph.Node

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

/**
 * Sensitive APIs
 */
internal val LINT_APIS = setOf(
        // <editor-fold desc="- Object">
        "java/lang/Object.wait()V",
        "java/lang/Object.wait(J)V",
        "java/lang/Object.wait(JI)V",
        // </editor-fold>
        // <editor-fold desc="- Thread">
        "java/lang/Thread.start()V",
        // </editor-fold>
        // <editor-fold desc="- ClassLoader">
        "java/lang/ClassLoader.getResource(Ljava/lang/String;)Ljava/net/URL;",
        "java/lang/ClassLoader.getResources(Ljava/lang/String;)Ljava/util/Enumeration;",
        "java/lang/ClassLoader.getResourceAsStream(Ljava/lang/String;)Ljava/io/InputStream;",
        "java/lang/ClassLoader.getSystemResource(Ljava/lang/String;)Ljava/net/URL;",
        "java/lang/ClassLoader.getSystemResources(Ljava/lang/String;)Ljava/util/Enumeration;",
        "java/lang/ClassLoader.getSystemResourceAsStream(Ljava/lang/String;)Ljava/io/InputStream;",
        // </editor-fold>
        // <editor-fold desc="- I/O">
        "java/io/FileDescriptor.sync()V",
        "java/io/InputStream.read()I",
        "java/io/InputStream.read([B)I",
        "java/io/InputStream.read([BII)I",
        "java/io/BufferedInputStream.read()I",
        "java/io/BufferedInputStream.read([BII)I",
        "java/io/OutputStream.write(I)V",
        "java/io/OutputStream.write([B)V",
        "java/io/OutputStream.write([BII)V",
        "java/io/OutputStream.flush()V",
        "java/io/BufferedOutputStream.write(I)V",
        "java/io/BufferedOutputStream.write([BII)V",
        "java/io/BufferedOutputStream.flush()V",
        "java/io/Reader.read()I",
        "java/io/Reader.read([C)I",
        "java/io/Reader.read([CII)I",
        "java/io/Reader.read(Ljava/nio/CharBuffer;)I",
        "java/io/Writer.append(C)Ljava/io/Writer;",
        "java/io/Writer.append(Ljava/lang/CharSequence;)Ljava/io/Writer;",
        "java/io/Writer.append(Ljava/lang/CharSequence;II)Ljava/io/Writer;",
        "java/io/Writer.flush()V",
        "java/io/Writer.write(I)V",
        "java/io/Writer.write(Ljava/lang/String;)V",
        "java/io/Writer.write(Ljava/lang/String;II)V",
        "java/io/Writer.write([C)V",
        "java/io/Writer.write([CII)V",
        // </editor-fold>
        // <editor-fold desc="- ServiceLoader">
        "java/util/ServiceLoader.load(Ljava/lang/Class;)Ljava/util/ServiceLoader;",
        "java/util/ServiceLoader.load(Ljava/lang/Class;Ljava/lang/ClassLoader;)Ljava/util/ServiceLoader;",
        // </editor-fold>
        // <editor-fold desc="- Zip/Jar File">
        "java/util/zip/ZipFile.<init>(Ljava/lang/String;)",
        "java/util/zip/ZipFile.getInputStream(Ljava/util/zip/ZipEntry;)",
        "java/util/jar/JarFile.<init>(Ljava/lang/String;)",
        "java/util/jar/JarFile.getInputStream(Ljava/util/jar/JarEntry;)",
        // </editor-fold>
        // <editor-fold desc="- SharedPreferences">
        "android/content/Context.getSharedPreferences(Ljava/lang/String;I)Landroid/content/SharedPreferences;",
        "android/content/SharedPreferences\$Editor.apply()V",
        "android/content/SharedPreferences\$Editor.commit()B",
        // </editor-fold>
        // <editor-fold desc="- AssetManager">
        "android/content/res/AssetManager.list(Ljava/lang/String;)[Ljava/lang/String;",
        "android/content/res/AssetManager.open(Ljava/lang/String;)Ljava/io/InputStream;",
        "android/content/res/AssetManager.open(Ljava/lang/String;I)Ljava/io/InputStream;",
        "android/content/res/AssetManager.openFd(Ljava/lang/String;)Landroid/content/res/AssetFileDescriptor;",
        "android/content/res/AssetManager.openNonAssetFd(Ljava/lang/String;)Landroid/content/res/AssetFileDescriptor;",
        "android/content/res/AssetManager.openNonAssetFd(ILjava/lang/String;)Landroid/content/res/AssetFileDescriptor;",
        "android/content/res/AssetManager.openXmlResourceParser(Ljava/lang/String;)Landroid/content/res/XmlResourceParser;",
        "android/content/res/AssetManager.openXmlResourceParser(ILjava/lang/String;)Landroid/content/res/XmlResourceParser;",
        // </editor-fold>
        // <editor-fold desc="- SQLiteDatabase">
        "android/database/sqlite/SQLiteDatabase.beginTransaction()V",
        "android/database/sqlite/SQLiteDatabase.beginTransactionNonExclusive()V",
        "android/database/sqlite/SQLiteDatabase.beginTransactionWithListener(Landroid/database/sqlite/SQLiteTransactionListener;)V",
        "android/database/sqlite/SQLiteDatabase.delete(Ljava/lang/String;Ljava/lang/String;[Ljava/lang/String;)V",
        "android/database/sqlite/SQLiteDatabase.deleteDatabase(Ljava/io/File;)Z",
        "android/database/sqlite/SQLiteDatabase.endTransaction()V",
        "android/database/sqlite/SQLiteDatabase.execSQL(Ljava/lang/String;)V",
        "android/database/sqlite/SQLiteDatabase.execSQL(Ljava/lang/String;[Ljava/lang/Object;)V",
        "android/database/sqlite/SQLiteDatabase.insert(Ljava/lang/String;Ljava/lang/String;Landroid/content/ContentValues;)J",
        "android/database/sqlite/SQLiteDatabase.insertOrThrow(Ljava/lang/String;Ljava/lang/String;Landroid/content/ContentValues;)J",
        "android/database/sqlite/SQLiteDatabase.insertWithOnConflict(Ljava/lang/String;Ljava/lang/String;Landroid/content/ContentValues;I)J",
        "android/database/sqlite/SQLiteDatabase.query(ZLjava/lang/String;[java/lang/String;Ljava/lang/String;[Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)Landroid/database/Cursor;",
        "android/database/sqlite/SQLiteDatabase.query(Ljava/lang/String;[java/lang/String;Ljava/lang/String;[Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)Landroid/database/Cursor;",
        "android/database/sqlite/SQLiteDatabase.query(ZLjava/lang/String;[java/lang/String;Ljava/lang/String;[Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Landroid/os/CancellationSignal;)Landroid/database/Cursor;",
        "android/database/sqlite/SQLiteDatabase.query(Ljava/lang/String;[java/lang/String;Ljava/lang/String;[Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)Landroid/database/Cursor;",
        "android/database/sqlite/SQLiteDatabase.queryWithFactory(Landroid/database/sqlite/SQLiteDatabase\$CursorFactory;ZLjava/lang/String;[java/lang/String;Ljava/lang/String;[Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Landroid/os/CancellationSignal;)Landroid/database/Cursor;",
        "android/database/sqlite/SQLiteDatabase.queryWithFactory(Landroid/database/sqlite/SQLiteDatabase\$CursorFactory;ZLjava/lang/String;[java/lang/String;Ljava/lang/String;[Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)Landroid/database/Cursor;",
        "android/database/sqlite/SQLiteDatabase.rawQuery(Ljava/lang/String;[Ljava/lang/String;Landroid/os/CancellationSignal;)Landroid/database/Cursor;",
        "android/database/sqlite/SQLiteDatabase.rawQuery(Ljava/lang/String;[Ljava/lang/String;)Landroid/database/Cursor;",
        "android/database/sqlite/SQLiteDatabase.rawQueryWithFactory(Landroid/database/sqlite/SQLiteDatabase\$CursorFactory;Ljava/lang/String;[java/lang/String;Ljava/lang/String;Landroid/os/CancellationSignal;)Landroid/database/Cursor;",
        "android/database/sqlite/SQLiteDatabase.rawQueryWithFactory(Landroid/database/sqlite/SQLiteDatabase\$CursorFactory;Ljava/lang/String;[java/lang/String;Ljava/lang/String;)Landroid/database/Cursor;",
        "android/database/sqlite/SQLiteDatabase.replace(Ljava/lang/String;Ljava/lang/String;Landroid/content/ContentValues;)J",
        "android/database/sqlite/SQLiteDatabase.replaceOrThrow(Ljava/lang/String;Ljava/lang/String;Landroid/content/ContentValues;)J",
        "android/database/sqlite/SQLiteDatabase.update(Ljava/lang/String;Ljava/lang/String;Landroid/content/ContentValues;)J",
        "android/database/sqlite/SQLiteDatabase.updateWithOnConflict(Ljava/lang/String;Landroid/content/ContentValues;Ljava/lang/String;[Ljava/lang/String;I)J",
        // </editor-fold>
        // <editor-fold desc="- BitmapFactory">
        "android/graphics/BitmapFactory.decodeByteArray([BIILandroid/graphics/BitmapFactory\$Options;)Landroid/graphics/Bitmap;",
        "android/graphics/BitmapFactory.decodeByteArray([BII)Landroid/graphics/Bitmap;",
        "android/graphics/BitmapFactory.decodeFile(Ljava/lang/String;Landroid/graphics/BitmapFactory\$Options;)Landroid/graphics/Bitmap;",
        "android/graphics/BitmapFactory.decodeFile(Ljava/lang/String;)Landroid/graphics/Bitmap;",
        "android/graphics/BitmapFactory.decodeFileDescriptor(Ljava/io/FileDescriptor;)Landroid/graphics/Bitmap;",
        "android/graphics/BitmapFactory.decodeFileDescriptor(Ljava/io/FileDescriptor;Landroid/graphics/Rect;Landroid/graphics/BitmapFactory\$Options;)Landroid/graphics/Bitmap;",
        "android/graphics/BitmapFactory.decodeResource(Landroid/content/res/Resources;I)Landroid/graphics/Bitmap;",
        "android/graphics/BitmapFactory.decodeResource(Landroid/content/res/Resources;ILandroid/graphics/BitmapFactory\$Options;)Landroid/graphics/Bitmap;",
        "android/graphics/BitmapFactory.decodeResourceStream(Landroid/content/res/Resources;Landroid/util/TypedValue;Ljava/io/InputStream;Landroid/graphics/Rect;Landroid/graphics/BitmapFactory\$Options;)Landroid/graphics/Bitmap;",
        "android/graphics/BitmapFactory.decodeStream(Ljava/io/InputStream;)Landroid/graphics/Bitmap;",
        "android/graphics/BitmapFactory.decodeStream(Ljava/io/InputStream;Landroid/graphics/Rect;Landroid/graphics/BitmapFactory\$Options;)Landroid/graphics/Bitmap;"
        // </editor-fold>
).map(Node.Companion::valueOf).toSet()
