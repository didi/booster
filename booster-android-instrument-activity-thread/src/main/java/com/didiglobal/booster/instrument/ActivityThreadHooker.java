package com.didiglobal.booster.instrument;

import android.os.Handler;
import android.util.Log;

import static com.didiglobal.booster.android.bugfix.Constants.TAG;
import static com.didiglobal.booster.android.bugfix.Reflection.getFieldValue;
import static com.didiglobal.booster.android.bugfix.Reflection.getStaticFieldValue;
import static com.didiglobal.booster.android.bugfix.Reflection.invokeMethod;
import static com.didiglobal.booster.android.bugfix.Reflection.setFieldValue;

/**
 * @author neighbWang
 */
public class ActivityThreadHooker {

    private volatile static boolean hooked;

    public static void hook() {
        if (hooked) {
            return;
        }

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

        if (null == thread) {
            Log.w(TAG, "ActivityThread instance is inaccessible");
            return;
        }

        try {
            final Handler handler = getHandler(thread);
            if (null == handler || !(hooked = setFieldValue(handler, "mCallback", new ActivityThreadCallback(handler)))) {
                Log.i(TAG, "Hook ActivityThread.mH.mCallback failed");
            }
        } catch (final Throwable t) {
            Log.w(TAG, "Hook ActivityThread.mH.mCallback failed", t);
        }
        if(hooked) {
            Log.i(TAG, "Hook ActivityThread.mH.mCallback success!");
        }
    }

    private static Handler getHandler(final Object thread) {
        Handler handler;

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
}
