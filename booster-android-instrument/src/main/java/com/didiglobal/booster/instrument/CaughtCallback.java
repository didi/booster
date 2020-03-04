package com.didiglobal.booster.instrument;

import android.os.Handler;
import android.os.Message;

public class CaughtCallback implements Handler.Callback {

    private final Handler mHandler;

    public CaughtCallback(final Handler handler) {
        this.mHandler = handler;
    }

    @Override
    public boolean handleMessage(final Message msg) {
        try {
            this.mHandler.handleMessage(msg);
        } catch (final RuntimeException e) {
            // ignore
        }
        return true;
    }
}
