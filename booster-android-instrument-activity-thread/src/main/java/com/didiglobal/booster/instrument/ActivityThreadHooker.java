package com.didiglobal.booster.instrument;

import android.util.Log;

import static com.didiglobal.booster.instrument.Constants.TAG;

/**
 * @author neighbWang
 */
public class ActivityThreadHooker {

    private volatile static boolean hooked;

    /**
     * @param ignorePackages comma-separated list
     */
    public static void hook(final String ignorePackages) {
        if (hooked) {
            return;
        }

        try {
            final String pkgs = null == ignorePackages ? "" : ignorePackages.trim();
            final ActivityThreadCallback callback = new ActivityThreadCallback(pkgs.split("\\s*,\\s*"));
            if (!(hooked = callback.hook())) {
                Log.i(TAG, "Hook ActivityThread.mH.mCallback failed");
            }
        } catch (final Throwable t) {
            Log.w(TAG, "Hook ActivityThread.mH.mCallback failed", t);
        }

        if (hooked) {
            Log.i(TAG, "Hook ActivityThread.mH.mCallback success!");
        }
    }

}
