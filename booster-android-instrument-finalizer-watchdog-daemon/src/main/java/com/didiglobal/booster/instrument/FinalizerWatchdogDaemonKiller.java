package com.didiglobal.booster.instrument;

import android.os.Looper;
import android.util.Log;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.TimeoutException;

import static com.didiglobal.booster.android.bugfix.Constants.TAG;

/**
 * Stop thread {@code FinalizerWatchdogDaemon} to prevent crash caused by {@link TimeoutException}
 *
 * @author neighbWang
 */
public class FinalizerWatchdogDaemonKiller {

    private static final int MAX_RETRY_TIMES = 10;

    private static final long THREAD_SLEEP_TIME = 5000;

    @SuppressWarnings("unchecked")
    public static void kill() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                for (int retry = 0; isFinalizerWatchdogDaemonExists() && retry < MAX_RETRY_TIMES; retry++) {
                    try {
                        final Class clazz = Class.forName("java.lang.Daemons$FinalizerWatchdogDaemon");
                        final Field field = clazz.getDeclaredField("INSTANCE");
                        field.setAccessible(true);
                        final Object watchdog = field.get(null);

                        try {
                            final Field thread = clazz.getSuperclass().getDeclaredField("thread");
                            thread.setAccessible(true);
                            thread.set(watchdog, null);
                        } catch (final Throwable t) {
                            Log.e(TAG, "Clearing reference of thread `FinalizerWatchdogDaemon` failed", t);

                            try {
                                final Method method = clazz.getSuperclass().getDeclaredMethod("stop");
                                method.setAccessible(true);
                                method.invoke(watchdog);
                            } catch (final Throwable e) {
                                Log.e(TAG, "Interrupting thread `FinalizerWatchdogDaemon` failed", e);
                                break;
                            }
                        }

                        try {
                            Thread.sleep(THREAD_SLEEP_TIME);
                        } catch (final InterruptedException ignore) {
                        }
                    } catch (final Throwable t) {
                        Log.e(TAG, "Killing thread `FinalizerWatchdogDaemon` failed", t);
                        break;
                    }
                }
                if (isFinalizerWatchdogDaemonExists()) {
                    Log.e(TAG, "Killing thread `FinalizerWatchdogDaemon` failed");
                } else {
                    Log.i(TAG, "Thread `FinalizerWatchdogDaemon` does not exist");
                }
            }
        }, "FinalizerWatchdogDaemonKiller").start();
    }

    private static boolean isFinalizerWatchdogDaemonExists() {
        try {
            for (final Thread t : getAllThreads()) {
                if (null != t && "FinalizerWatchdogDaemon".equals(t.getName())) {
                    return true;
                }
            }
        } catch (final Throwable ignore) {
        }
        return false;
    }

    private static Collection<Thread> getAllThreads() {
        try {
            final ThreadGroup root = Looper.getMainLooper().getThread().getThreadGroup().getParent();
            final Thread[] src = new Thread[root.activeCount()];
            final int n = root.enumerate(src);

            if (n != src.length) {
                final Thread[] target = new Thread[n];
                System.arraycopy(src, 0, target, 0, n);
                return Arrays.asList(target);
            } else {
                return Arrays.asList(src);
            }
        } catch (final Throwable t) {
            return Thread.getAllStackTraces().keySet();
        }
    }
}
