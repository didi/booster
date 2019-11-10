package com.didiglobal.booster.instrument.sharedpreferences.io;

import java.io.Closeable;
import java.io.IOException;

/**
 * @author neighbWang
 */
public final class IoUtils {

    private IoUtils() {
    }

    public static void close(Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (IOException ignored) {
            }
        }
    }
}
