package com.didiglobal.booster.instrument;

import android.util.Log;

import java.util.concurrent.ThreadPoolExecutor;

import static com.didiglobal.booster.instrument.Constants.TAG;

/**
 * @author neighbWang
 */
public class ShadowAsyncTask {

    /**
     * Optimize the thread pool executor of AsyncTask with {@code allowCoreThreadTimeOut = true}
     */
    public static void optimizeAsyncTaskExecutor() {
        try {
            final ThreadPoolExecutor executor = ((ThreadPoolExecutor) android.os.AsyncTask.THREAD_POOL_EXECUTOR);
            executor.allowCoreThreadTimeOut(true);
            Log.i(TAG, "Optimize AsyncTask executor success！");
        } catch (final Throwable t) {
            Log.e(TAG, "Optimize AsyncTask executor error: allowCoreThreadTimeOut = true", t);
        }
    }
}