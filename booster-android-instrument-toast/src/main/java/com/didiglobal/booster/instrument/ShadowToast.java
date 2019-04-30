package com.didiglobal.booster.instrument;

import android.os.Build;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;
import com.didiglobal.booster.android.bugfix.CaughtCallback;
import com.didiglobal.booster.android.bugfix.CaughtRunnable;

import static com.didiglobal.booster.android.bugfix.Constants.TAG;
import static com.didiglobal.booster.android.bugfix.Reflection.getFieldValue;
import static com.didiglobal.booster.android.bugfix.Reflection.setFieldValue;

public class ShadowToast {

    /**
     * Fix {@code WindowManager$BadTokenException} for Android N
     *
     * @param toast
     *         The original toast
     */
    public static void show(final Toast toast) {
        if (Build.VERSION.SDK_INT == 25) {
            workaround(toast).show();
        } else {
            toast.show();
        }
    }

    private static Toast workaround(final Toast toast) {
        final Object tn = getFieldValue(toast, "mTN");
        if (null == tn) {
            Log.w(TAG, "Field mTN of " + toast + " is null");
            return toast;
        }

        final Object handler = getFieldValue(tn, "mHandler");
        if (handler instanceof Handler) {
            if (setFieldValue(handler, "mCallback", new CaughtCallback((Handler) handler))) {
                return toast;
            }
        }

        final Object show = getFieldValue(tn, "mShow");
        if (show instanceof Runnable) {
            if (setFieldValue(tn, "mShow", new CaughtRunnable((Runnable) show))) {
                return toast;
            }
        }

        Log.w(TAG, "Neither field mHandler nor mShow of " + tn + " is accessible");
        return toast;
    }

}
