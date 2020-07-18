package com.didiglobal.booster.instrument;

import java.util.Timer;

import static com.didiglobal.booster.instrument.ShadowThread.makeThreadName;

/**
 * @author johnsonlee
 */
public class ShadowTimer extends Timer {

    public static Timer newTimer(final String name) {
        return new Timer(name);
    }

    public static Timer newTimer(final String name, final String prefix) {
        return new Timer(makeThreadName(name, prefix));
    }

    public static Timer newTimer(final boolean isDaemon, final String name) {
        return new Timer(name, isDaemon);
    }

    public static Timer newTimer(final String name, final boolean isDaemon, final String prefix) {
        return new Timer(makeThreadName(name, prefix), isDaemon);
    }

    /**
     * Initialize {@code Timer} with new name, this constructor is used by {@code ThreadTransformer} for thread renaming
     *
     * @param prefix the prefix of new name
     */
    public ShadowTimer(final String prefix) {
        super(prefix);
    }

    /**
     * Initialize {@code Timer} with new name, this constructor is used by {@code ThreadTransformer} for thread renaming
     *
     * @param isDaemon true if the associated thread should run as a daemon
     * @param prefix   the prefix of new name
     */
    public ShadowTimer(final boolean isDaemon, final String prefix) {
        super(prefix, isDaemon);
    }

    /**
     * Initialize {@code Timer} with new name, this constructor is used by {@code ThreadTransformer} for thread renaming
     *
     * @param name   the original name
     * @param prefix the prefix of new name
     */
    public ShadowTimer(final String name, final String prefix) {
        super(makeThreadName(name, prefix));
    }

    /**
     * Initialize {@code Timer} with new name, this constructor is used by {@code ThreadTransformer} for thread renaming
     *
     * @param name     the original name
     * @param isDaemon true if the associated thread should run as a daemon
     * @param prefix   the prefix of new name
     */
    public ShadowTimer(final String name, final boolean isDaemon, final String prefix) {
        super(makeThreadName(name, prefix), isDaemon);
    }

}
