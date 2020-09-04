package com.didiglobal.booster.instrument.sharedpreferences;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;
import com.didiglobal.booster.instrument.ShadowExecutors;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author neighbWang
 */
public final class BoosterSharedPreferences implements SharedPreferences {
    private static final ExecutorService SYNC_EXECUTOR = Executors.newCachedThreadPool();
    private static final Map<String, BoosterSharedPreferences> sSharedPreferencesMap = new ConcurrentHashMap<>();
    private static final Object SENTINEL = new Object();

    private final WeakHashMap<OnSharedPreferenceChangeListener, Object> mListeners = new WeakHashMap<>();
    private final Object mLock = new Object();
    private final Object mLoadLock = new Object();
    private final ExecutorService mWriteExecutor;
    private final SharedPreferencesManager mManager;

    private volatile boolean mLoaded = false;
    private Map<String, Object> mKeyValueMap = new ConcurrentHashMap<>();

    private BoosterSharedPreferences(final Context context, final String name) {
        mWriteExecutor = ShadowExecutors.newOptimizedSingleThreadExecutor(name);
        mManager = new SharedPreferencesManager(context, name);
        startLoadFromDisk();
    }

    private void startLoadFromDisk() {
        synchronized (mLoadLock) {
            mLoaded = false;
        }
        SYNC_EXECUTOR.submit(new LoadThread());
    }

    public static SharedPreferences getSharedPreferences(final Context context, final String name) {
        if (context == null) {
            throw new IllegalArgumentException("Context can not be null!");
        }
        if (!sSharedPreferencesMap.containsKey(name)) {
            sSharedPreferencesMap.put(name, new BoosterSharedPreferences(context, name));
        }
        return sSharedPreferencesMap.get(name);
    }

    @Override
    public Map<String, ?> getAll() {
        awaitLoadedFromDisk();
        return new HashMap<>(mKeyValueMap);
    }

    @Override
    public String getString(final String key, final String defaultValue) {
        awaitLoadedFromDisk();
        final Object v = mKeyValueMap.get(key);
        return v != null ? (String) v : defaultValue;
    }

    private void awaitLoadedFromDisk() {
        synchronized (mLoadLock) {
            while (!mLoaded) {
                try {
                    mLoadLock.wait();
                } catch (InterruptedException ignored) {
                }
            }
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public Set<String> getStringSet(final String key, final Set<String> defValues) {
        awaitLoadedFromDisk();
        final Object v = mKeyValueMap.get(key);
        return v != null ? (Set<String>) v : defValues;
    }

    @Override
    public int getInt(final String key, final int defValue) {
        awaitLoadedFromDisk();
        final Object v = mKeyValueMap.get(key);
        return v != null ? (Integer) v : defValue;
    }

    @Override
    public long getLong(final String key, final long defValue) {
        awaitLoadedFromDisk();
        final Object v = mKeyValueMap.get(key);
        return v != null ? (Long) v : defValue;
    }

    @Override
    public float getFloat(final String key, final float defValue) {
        awaitLoadedFromDisk();
        final Object v = mKeyValueMap.get(key);
        return v != null ? (Float) v : defValue;
    }

    @Override
    public boolean getBoolean(final String key, final boolean defValue) {
        awaitLoadedFromDisk();
        final Object v = mKeyValueMap.get(key);
        return v != null ? (Boolean) v : defValue;
    }

    @Override
    public boolean contains(final String key) {
        awaitLoadedFromDisk();
        return mKeyValueMap.containsKey(key);
    }

    @Override
    public Editor edit() {
        return new BoosterEditor();
    }

    @Override
    public void registerOnSharedPreferenceChangeListener(final OnSharedPreferenceChangeListener onSharedPreferenceChangeListener) {
        if (onSharedPreferenceChangeListener != null) {
            this.mListeners.put(onSharedPreferenceChangeListener, null);
        }
    }

    @Override
    public void unregisterOnSharedPreferenceChangeListener(final OnSharedPreferenceChangeListener onSharedPreferenceChangeListener) {
        if (onSharedPreferenceChangeListener != null) {
            this.mListeners.remove(onSharedPreferenceChangeListener);
        }
    }

    private void loadFromXml() {
        synchronized (mLock) {
            final Map<String, Object> loadedData = mManager.read();
            this.mKeyValueMap.clear();
            if (loadedData != null) {
                for (final Map.Entry<String, Object> entry : loadedData.entrySet()) {
                    final String key = entry.getKey();
                    final Object value = entry.getValue();
                    if (key != null && value != null) {
                        mKeyValueMap.put(key, value);
                    }
                }
            }
        }
        synchronized (mLoadLock) {
            mLoaded = true;
            mLoadLock.notifyAll();
        }
    }

    private class BoosterEditor implements Editor {

        private final Map<String, Object> mModifies = new HashMap<>();
        private volatile boolean mClear = false;

        private BoosterEditor() {
        }

        @Override
        public Editor putString(final String key, final String value) {
            return put(key, value);
        }

        @Override
        public Editor putStringSet(final String key, final Set<String> values) {
            return put(key, new HashSet<>(values));
        }

        @Override
        public Editor putInt(final String key, final int value) {
            return put(key, value);
        }

        @Override
        public Editor putLong(final String key, final long value) {
            return put(key, value);
        }

        @Override
        public Editor putFloat(final String key, final float value) {
            return put(key, value);
        }

        @Override
        public Editor putBoolean(final String key, final boolean value) {
            return put(key, value);
        }

        @Override
        public Editor remove(final String key) {
            return put(key, SENTINEL);
        }

        private synchronized Editor put(final String key, final Object value) {
            mModifies.put(key, value);
            return this;
        }

        @Override
        public synchronized Editor clear() {
            mClear = true;
            return this;
        }

        @Override
        public boolean commit() {
            sync();
            return true;
        }

        @Override
        public void apply() {
            sync();
        }

        private synchronized void sync() {
            final Map<String, Object> modifies = new HashMap<>(mModifies);
            mModifies.clear();
            awaitLoadedFromDisk();
            final Map<String, Object> mapToWriteToDisk = new HashMap<>(mKeyValueMap);
            if (mClear) {
                mapToWriteToDisk.clear();
                mClear = false;
            }
            final boolean hasListeners = mListeners.size() > 0;
            final List<String> modifiedKeys = new ArrayList<>();
            for (final Map.Entry<String, Object> e : modifies.entrySet()) {
                final String k = e.getKey();
                final Object v = e.getValue();
                if (v == SENTINEL || v == null) {
                    if (!mapToWriteToDisk.containsKey(k)) {
                        continue;
                    }
                    mapToWriteToDisk.remove(k);
                } else {
                    if (mapToWriteToDisk.containsKey(k)) {
                        final Object existingValue = mapToWriteToDisk.get(k);
                        if (existingValue != null && existingValue.equals(v)) {
                            continue;
                        }
                    }
                    mapToWriteToDisk.put(k, v);
                }
                if (hasListeners) {
                    modifiedKeys.add(k);
                }
            }
            mKeyValueMap.clear();
            mKeyValueMap.putAll(mapToWriteToDisk);
            mWriteExecutor.execute(new SyncTask(hasListeners, modifiedKeys, mapToWriteToDisk));
        }

        private class SyncTask implements Runnable {
            private final boolean mNeedNotifyListener;
            private final List<String> mModifiedKeyList;
            private final Map<String, Object> mMap;

            SyncTask(boolean needNotifyListener, List<String> modifiedKeys, Map<String, Object> map) {
                this.mNeedNotifyListener = needNotifyListener;
                this.mModifiedKeyList = modifiedKeys;
                this.mMap = map;
            }

            @Override
            public void run() {
                synchronized (mLock) {
                    if (mManager.write(mMap) && mNeedNotifyListener) {
                        notifyListeners(mModifiedKeyList);
                    }
                }
            }

            private void notifyListeners(final Collection<String> keys) {
                if (Looper.myLooper() == Looper.getMainLooper()) {
                    final OnSharedPreferenceChangeListener[] listeners = mListeners.keySet().toArray(new OnSharedPreferenceChangeListener[0]);
                    for (final OnSharedPreferenceChangeListener listener : listeners) {
                        for (String key : keys) {
                            listener.onSharedPreferenceChanged(BoosterSharedPreferences.this, key);
                        }
                    }
                } else {
                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            notifyListeners(keys);
                        }
                    });
                }

            }
        }
    }

    private class LoadThread implements Runnable {
        @Override
        public void run() {
            loadFromXml();
        }
    }
}
