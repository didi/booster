package com.didiglobal.booster.instrument;

public class ShadowThread {

    /**
     * {@code U+200B}: Zero-Width Space
     */
    static final String MARK = "\u200B";

    public static Thread newThread(final String prefix) {
        return new Thread(prefix);
    }

    public static Thread newThread(final Runnable target, final String prefix) {
        return new Thread(target, prefix);
    }

    public static Thread newThread(final ThreadGroup group, final Runnable target, final String prefix) {
        return new Thread(group, target, prefix);
    }

    public static Thread newThread(final String name, final String prefix) {
        return new Thread(makeThreadName(name, prefix));
    }

    public static Thread newThread(final ThreadGroup group, final String name, final String prefix) {
        return new Thread(group, makeThreadName(name, prefix));
    }

    public static Thread newThread(final Runnable target, final String name, final String prefix) {
        return new Thread(target, makeThreadName(name, prefix));
    }

    public static Thread newThread(final ThreadGroup group, final Runnable target, final String name, final String prefix) {
        return new Thread(group, target, makeThreadName(name, prefix));
    }

    public static Thread newThread(final ThreadGroup group, final Runnable target, final String name, final long stackSize, final String prefix) {
        return new Thread(group, target, makeThreadName(name, prefix), stackSize);
    }

    public static Thread setThreadName(final Thread t, final String prefix) {
        t.setName(makeThreadName(t.getName(), prefix));
        return t;
    }

    public static String makeThreadName(final String name) {
        return MARK;
    }

    public static String makeThreadName(final String name, final String prefix) {
        return name == null ? prefix : (name.startsWith(MARK) ? name : (prefix + "#" + name));
    }

}
