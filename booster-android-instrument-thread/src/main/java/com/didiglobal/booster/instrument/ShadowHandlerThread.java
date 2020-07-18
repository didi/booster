package com.didiglobal.booster.instrument;

import android.os.HandlerThread;

import static com.didiglobal.booster.instrument.ShadowThread.makeThreadName;
import static java.lang.Math.min;

/**
 * @author johnsonlee
 */
public class ShadowHandlerThread extends HandlerThread {

    public static HandlerThread newHandlerThread(final String name, final String prefix) {
        return new HandlerThread(makeThreadName(name, prefix));
    }

    public static HandlerThread newHandlerThread(final String name, int priority, final String prefix) {
        return new HandlerThread(makeThreadName(name, prefix), min(android.os.Process.THREAD_PRIORITY_DEFAULT, priority));
    }

    /**
     * Initialize {@code HandlerThread} with new name, this constructor is used by {@code ThreadTransformer} for renaming
     *
     * @param name   the original name
     * @param prefix the prefix of new name
     */
    public ShadowHandlerThread(final String name, final String prefix) {
        super(makeThreadName(name, prefix));
    }

    /**
     * Initialize {@code HandlerThread} with new name, this constructor is used by {@code ThreadTransformer} for renaming
     *
     * @param name     the original name
     * @param priority the thread priority
     * @param prefix   the prefix of new name
     */
    public ShadowHandlerThread(final String name, final int priority, final String prefix) {
        super(makeThreadName(name, prefix), priority);
    }

}
