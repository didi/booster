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
        return BoosterSharedPreferences.getSharedPreferences(context, name);
    }

    public static SharedPreferences getDefaultSharedPreferences(Context context) {
        return BoosterSharedPreferences.getSharedPreferences(context, getDefaultSharedPreferencesName(context));
    }

    private static String getDefaultSharedPreferencesName(Context context) {
        return context.getPackageName() + "_preferences";
    }

    public static SharedPreferences getPreferences(final Activity activity, final int mode) {
        return getSharedPreferences(activity.getApplicationContext(), activity.getLocalClassName(), mode);
    }

}
