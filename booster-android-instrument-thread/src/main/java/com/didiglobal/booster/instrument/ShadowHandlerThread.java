package com.didiglobal.booster.instrument;

import android.os.HandlerThread;

import static com.didiglobal.booster.instrument.ShadowThread.makeThreadName;
import static java.lang.Math.min;

public class ShadowHandlerThread {

    public static HandlerThread newHandlerThread(final String name, final String prefix) {
        return new HandlerThread(makeThreadName(name, prefix));
    }

    public static HandlerThread newHandlerThread(final String name, int priority, final String prefix) {
        return new HandlerThread(makeThreadName(name, prefix), min(android.os.Process.THREAD_PRIORITY_DEFAULT, priority));
    }

}
