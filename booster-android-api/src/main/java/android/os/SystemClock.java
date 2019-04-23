package android.os;

public final class SystemClock {

    public static void sleep(long ms) {
        throw new RuntimeException("Stub!");
    }

    /**
     * Sets the current wall time, in milliseconds.  Requires the calling
     * process to have appropriate permissions.
     *
     * @return if the clock was successfully set to the specified time.
     */
    native public static boolean setCurrentTimeMillis(long millis);

    /**
     * Returns milliseconds since boot, not counting time spent in deep sleep.
     *
     * @return milliseconds of non-sleep uptime since boot.
     */
    native public static long uptimeMillis();

    /**
     * Returns milliseconds since boot, including time spent in sleep.
     *
     * @return elapsed milliseconds since boot.
     */
    native public static long elapsedRealtime();

    /**
     * Returns nanoseconds since boot, including time spent in sleep.
     *
     * @return elapsed nanoseconds since boot.
     */
    native public static long elapsedRealtimeNanos();

    /**
     * Returns milliseconds running in the current thread.
     *
     * @return elapsed milliseconds in the thread
     */
    native public static long currentThreadTimeMillis();

}
