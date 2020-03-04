package com.didiglobal.booster.instrument;

import android.content.res.Resources;
import android.os.Build;
import android.os.DeadSystemException;
import android.os.Handler;
import android.os.Message;
import android.os.Process;
import android.util.AndroidRuntimeException;
import android.util.Log;
import android.view.WindowManager;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static com.didiglobal.booster.instrument.Constants.TAG;
import static com.didiglobal.booster.instrument.Reflection.getFieldValue;
import static com.didiglobal.booster.instrument.Reflection.getStaticFieldValue;
import static com.didiglobal.booster.instrument.Reflection.invokeMethod;
import static com.didiglobal.booster.instrument.Reflection.setFieldValue;

/**
 * Callback used to catch ActivityThread exception caused by system.
 *
 * @author neighbWang
 */
class ActivityThreadCallback implements Handler.Callback {

    private static final String LOADED_APK_GET_ASSETS = "android.app.LoadedApk.getAssets";

    private static final String ASSET_MANAGER_GET_RESOURCE_VALUE = "android.content.res.AssetManager.getResourceValue";

    private static final String[] SYSTEM_PACKAGE_PREFIXES = {
            "java.",
            "android.",
            "androidx.",
            "dalvik.",
            "com.android.",
            "com.didiglobal.booster.instrument."
    };

    private final Handler mHandler;

    private final Handler.Callback mDelegate;

    public ActivityThreadCallback() {
        this.mHandler = getHandler(getActivityThread());
        this.mDelegate = getFieldValue(this.mHandler, "mCallback");
    }

    @Override
    public final boolean handleMessage(final Message msg) {
        try {
            if (null != mDelegate) {
                return this.mDelegate.handleMessage(msg);
            }

            if (null != this.mHandler) {
                this.mHandler.handleMessage(msg);
            }
        } catch (final NullPointerException e) {
            if (hasStackTraceElement(e, ASSET_MANAGER_GET_RESOURCE_VALUE, LOADED_APK_GET_ASSETS)) {
                return abort(e);
            }
            rethrowIfNotCausedBySystem(e);
        } catch (final SecurityException
                | IllegalArgumentException
                | AndroidRuntimeException
                | WindowManager.BadTokenException e) {
            rethrowIfNotCausedBySystem(e);
        } catch (final Resources.NotFoundException e) {
            rethrowIfNotCausedBySystem(e);
            return abort(e);
        } catch (final RuntimeException e) {
            final Throwable cause = e.getCause();
            if (((Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) && isCausedBy(cause, DeadSystemException.class))
                    || (isCausedBy(cause, NullPointerException.class) && hasStackTraceElement(e, LOADED_APK_GET_ASSETS))) {
                return abort(e);
            }
            rethrowIfNotCausedBySystem(e);
        } catch (final Error e) {
            rethrowIfNotCausedBySystem(e);
            return abort(e);
        }

        return true;
    }

    private static void rethrowIfNotCausedBySystem(final RuntimeException e) {
        if (!isCausedBySystem(e)) {
            throw e;
        }
    }

    private static void rethrowIfNotCausedBySystem(final Error e) {
        if (!isCausedBySystem(e)) {
            throw e;
        }
    }

    private static boolean isCausedBySystem(final Throwable t) {
        if (null == t) {
            return false;
        }

        for (Throwable cause = t; null != cause; cause = cause.getCause()) {
            for (final StackTraceElement element : cause.getStackTrace()) {
                if (!isSystemStackTrace(element)) {
                    return false;
                }
            }
        }

        return true;
    }

    private static boolean isSystemStackTrace(final StackTraceElement element) {
        final String name = element.getClassName();
        for (final String prefix : SYSTEM_PACKAGE_PREFIXES) {
            if (name.startsWith(prefix)) {
                return true;
            }
        }
        return false;
    }

    private static boolean hasStackTraceElement(final Throwable t, final String... traces) {
        return hasStackTraceElement(t, new HashSet<>(Arrays.asList(traces)));
    }

    private static boolean hasStackTraceElement(final Throwable t, final Set<String> traces) {
        if (null == t || null == traces || traces.isEmpty()) {
            return false;
        }

        for (final StackTraceElement element : t.getStackTrace()) {
            if (traces.contains(element.getClassName() + "." + element.getMethodName())) {
                return true;
            }
        }

        return hasStackTraceElement(t.getCause(), traces);
    }

    @SafeVarargs
    private static boolean isCausedBy(final Throwable t, final Class<? extends Throwable>... causes) {
        return isCausedBy(t, new HashSet<>(Arrays.asList(causes)));
    }

    private static boolean isCausedBy(final Throwable t, final Set<Class<? extends Throwable>> causes) {
        if (null == t) {
            return false;
        }

        if (causes.contains(t.getClass())) {
            return true;
        }

        return isCausedBy(t.getCause(), causes);
    }

    private static boolean abort(final Throwable t) {
        final int pid = Process.myPid();
        final String msg = "Process " + pid + " is going to be killed";

        if (null != t) {
            Log.w(TAG, msg, t);
        } else {
            Log.w(TAG, msg);
        }

        Process.killProcess(pid);
        System.exit(10);
        return true;
    }

    private static Handler getHandler(final Object thread) {
        Handler handler;

        if (null == thread) {
            return null;
        }

        if (null != (handler = getFieldValue(thread, "mH"))) {
            return handler;
        }

        if (null != (handler = invokeMethod(thread, "getHandler"))) {
            return handler;
        }

        try {
            if (null != (handler = getFieldValue(thread, Class.forName("android.app.ActivityThread$H")))) {
                return handler;
            }
        } catch (final ClassNotFoundException e) {
            Log.w(TAG, "Main thread handler is inaccessible", e);
        }

        return null;
    }

    private static Object getActivityThread() {
        Object thread = null;

        try {
            thread = android.app.ActivityThread.currentActivityThread();
        } catch (final Throwable t1) {
            Log.w(TAG, "ActivityThread.currentActivityThread() is inaccessible", t1);
            try {
                thread = getStaticFieldValue(android.app.ActivityThread.class, "sCurrentActivityThread");
            } catch (final Throwable t2) {
                Log.w(TAG, "ActivityThread.sCurrentActivityThread is inaccessible", t1);
            }
        }

        if (null != thread) {
            return thread;
        }

        Log.w(TAG, "ActivityThread instance is inaccessible");
        return null;
    }

    boolean hook() {
        return setFieldValue(this.mHandler, "mCallback", this);
    }
}
