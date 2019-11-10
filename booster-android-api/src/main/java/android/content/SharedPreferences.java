package android.content;

import java.util.Map;
import java.util.Set;

public interface SharedPreferences {
    Map<String, ?> getAll();

    String getString(String var1, String var2);

    Set<String> getStringSet(String var1, Set<String> var2);

    int getInt(String var1, int var2);

    long getLong(String var1, long var2);

    float getFloat(String var1, float var2);

    boolean getBoolean(String var1, boolean var2);

    boolean contains(String var1);

    SharedPreferences.Editor edit();

    void registerOnSharedPreferenceChangeListener(SharedPreferences.OnSharedPreferenceChangeListener var1);

    void unregisterOnSharedPreferenceChangeListener(SharedPreferences.OnSharedPreferenceChangeListener var1);

    public interface OnSharedPreferenceChangeListener {
        void onSharedPreferenceChanged(SharedPreferences var1, String var2);
    }

    public interface Editor {
        SharedPreferences.Editor putString(String var1,  String var2);

        SharedPreferences.Editor putStringSet(String var1, Set<String> var2);

        SharedPreferences.Editor putInt(String var1, int var2);

        SharedPreferences.Editor putLong(String var1, long var2);

        SharedPreferences.Editor putFloat(String var1, float var2);

        SharedPreferences.Editor putBoolean(String var1, boolean var2);

        SharedPreferences.Editor remove(String var1);

        SharedPreferences.Editor clear();

        boolean commit();

        void apply();
    }
}