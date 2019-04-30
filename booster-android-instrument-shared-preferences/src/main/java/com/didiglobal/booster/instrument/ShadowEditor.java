package com.didiglobal.booster.instrument;

import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Looper;

public class ShadowEditor {

    public static void apply(final SharedPreferences.Editor editor) {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            AsyncTask.SERIAL_EXECUTOR.execute(new Runnable() {
                @Override
                public void run() {
                    editor.commit();
                }
            });
        } else {
            editor.commit();
        }
    }

}
