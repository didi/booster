package com.didiglobal.booster.instrument;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;
import com.didiglobal.booster.instrument.sharedpreferences.BoosterSharedPreferences;

public class ShadowSharedPreferences {

    public static SharedPreferences getSharedPreferences(final Context context, String name, final int mode) {
        if (TextUtils.isEmpty(name)) {
            name = "null";
        }
        return BoosterSharedPreferences.getSharedPreferences(name);
    }

    public static SharedPreferences getPreferences(final Activity activity, final int mode) {
        return getSharedPreferences(activity.getApplicationContext(), activity.getLocalClassName(), mode);
    }

}
