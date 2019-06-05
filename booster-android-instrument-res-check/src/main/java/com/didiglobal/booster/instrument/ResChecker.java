package com.didiglobal.booster.instrument;

import android.app.Application;
import android.os.Process;
import android.util.Log;

import static com.didiglobal.booster.android.bugfix.Constants.TAG;

/**
 * @author neighbWang
 */
public class ResChecker {

    public static void checkRes(final Application app) {
        if (null == app.getAssets() || null == app.getResources()) {
            final int pid = Process.myPid();
            Log.w(TAG, "Process " + pid + " is going to be killed");
            Process.killProcess(pid);
            System.exit(10);
        }
    }
}
