package com.didiglobal.booster.instrument;

import java.io.OutputStream;
import java.io.PrintStream;

/**
 *
 * @author neighbWang
 */
public final class ShadowSystem {

    public static final PrintStream out = new PrintStream(new OutputStream() {
        @Override
        public void write(final int b) {
        }
    });

    public static final PrintStream err = out;

    private ShadowSystem() {
    }

}
