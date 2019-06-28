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

import static com.didiglobal.booster.android.bugfix.Constants.TAG;

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
            ActivityThreadCallback.class.getPackage().getName() + "."
    };

    private final Handler mHandler;

    public ActivityThreadCallback(final Handler handler) {
        this.mHandler = handler;
    }

    @Override
    public final boolean handleMessage(final Message msg) {
        try {
            this.mHandler.handleMessage(msg);
        } catch (final NullPointerException e) {
            if (hasStackTraceElement(e, ASSET_MANAGER_GET_RESOURCE_VALUE, LOADED_APK_GET_ASSETS)) {
                abort(e);
            }
            rethrowIfNotCausedBySystem(e);
        } catch (final SecurityException
                | IllegalArgumentException
                | AndroidRuntimeException
                | WindowManager.BadTokenException e) {
            rethrowIfNotCausedBySystem(e);
        } catch (final Resources.NotFoundException e) {
            rethrowIfNotCausedBySystem(e);
            abort(e);
        } catch (final RuntimeException e) {
            final Throwable cause = e.getCause();
            if (((Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) && isCausedBy(cause, DeadSystemException.class))
                    || (isCausedBy(cause, NullPointerException.class) && hasStackTraceElement(e, LOADED_APK_GET_ASSETS))) {
                abort(e);
            }
            rethrowIfNotCausedBySystem(e);
        } catch (final Error e) {
            rethrowIfNotCausedBySystem(e);
            abort(e);
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

    private static void abort(final Throwable t) {
        final int pid = Process.myPid();
        final String msg = "Process " + pid + " is going to be killed";

        if (null != t) {
            Log.w(TAG, msg, t);
        } else {
            Log.w(TAG, msg);
        }

        Process.killProcess(pid);
        System.exit(10);
    }

}
