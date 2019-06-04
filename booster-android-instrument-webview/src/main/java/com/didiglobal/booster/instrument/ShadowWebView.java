package com.didiglobal.booster.instrument;

import android.app.Application;
import android.os.MessageQueue;
import android.os.SystemClock;
import android.util.Log;

import static com.didiglobal.booster.android.bugfix.Constants.TAG;
import static com.didiglobal.booster.android.bugfix.Reflection.invokeMethod;
import static com.didiglobal.booster.android.bugfix.Reflection.invokeStaticMethod;

/**
 * @author neighbWang
 */
public class ShadowWebView {

    public static void preloadWebView(final Application app) {
        try {
            app.getMainLooper().getQueue().addIdleHandler(new MessageQueue.IdleHandler() {
                @Override
                public boolean queueIdle() {
                    startChromiumEngine();
                    return false;
                }
            });
        } catch (final Throwable t) {
            Log.e(TAG, "Oops!", t);
        }
    }

    private static void startChromiumEngine() {
        try {
            final long t0 = SystemClock.uptimeMillis();
            final Object provider = invokeStaticMethod(Class.forName("android.webkit.WebViewFactory"), "getProvider");
            invokeMethod(provider, "startYourEngines", new Class[]{boolean.class}, new Object[]{true});
            Log.i(TAG, "Start chromium engine complete: " + (SystemClock.uptimeMillis() - t0) + " ms");
        } catch (final Throwable t) {
            Log.e(TAG, "Start chromium engine error", t);
        }
    }
}