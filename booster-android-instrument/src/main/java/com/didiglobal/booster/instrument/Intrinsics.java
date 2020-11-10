package com.didiglobal.booster.instrument;

import java.util.ArrayList;

public abstract class Intrinsics {

    public static <T extends Throwable> T sanitizeStackTrace(T throwable, Class<?> clazz) {
        return sanitizeStackTrace(throwable, clazz.getName());
    }

    public static <T extends Throwable> T sanitizeStackTrace(T throwable, String classNameToDrop) {
        final StackTraceElement[] stackTrace = throwable.getStackTrace();
        final ArrayList<StackTraceElement> newStackTrace = new ArrayList<>(stackTrace.length);

        for (StackTraceElement ste : stackTrace) {
            if (!classNameToDrop.equals(ste.getClassName())) {
                newStackTrace.add(ste);
            }
        }

        throwable.setStackTrace(newStackTrace.toArray(new StackTraceElement[0]));
        return throwable;
    }

    private Intrinsics() {
    }

}
