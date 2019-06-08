package com.didiglobal.booster.android.bugfix;

public interface Constants {

    String TAG = "booster";

    String LOADED_APK_GET_ASSETS = "android.app.LoadedApk.getAssets";

    String ASSET_MANAGER_GET_RESOURCE_VALUE = "android.content.res.AssetManager.getResourceValue";

    String[] SYSTEM_PACKAGE_PREFIXES = {
            "java.",
            "android.",
            "dalvik.",
            "com.android.",
            Constants.class.getPackage().getName() + "."
    };

}
