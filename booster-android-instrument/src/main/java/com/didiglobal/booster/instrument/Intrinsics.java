package com.didiglobal.booster.instrument;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;

public abstract class Intrinsics {

    public static <T extends Throwable> T sanitizeStackTrace(T throwable, Class<?> clazz) {
        return sanitizeStackTrace(throwable, clazz.getName());
    }

    @SuppressWarnings("Java8CollectionRemoveIf")
    public static <T extends Throwable> T sanitizeStackTrace(T throwable, String classNameToDrop) {
        final StackTraceElement[] stackTrace = throwable.getStackTrace();
        final ArrayList<StackTraceElement> newStackTrace = new ArrayList<>(Arrays.asList(stackTrace));

        for (final Iterator<StackTraceElement> i = newStackTrace.iterator(); i.hasNext();) {
            final StackTraceElement ste = i.next();
            if (classNameToDrop.equals(ste.getClassName())) {
                i.remove();
            }
        }

        if (stackTrace.length != newStackTrace.size()) {
            throwable.setStackTrace(newStackTrace.toArray(new StackTraceElement[0]));
        }

        return throwable;
    }

    private Intrinsics() {
    }

}
