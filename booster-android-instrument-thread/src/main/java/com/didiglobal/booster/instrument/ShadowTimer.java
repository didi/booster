package com.didiglobal.booster.instrument;

import java.util.Timer;

import static com.didiglobal.booster.instrument.ShadowThread.makeThreadName;

public class ShadowTimer {

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

}
